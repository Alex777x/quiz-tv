package com.clean.quiztv.game

import com.clean.quiztv.audio.TvSoundManager
import com.clean.quiztv.data.IQuestionPackRepository
import com.clean.quiztv.domain.model.AppLanguage
import com.clean.quiztv.domain.model.GameFormat
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.LobbyStep
import com.clean.quiztv.domain.model.Player
import com.clean.quiztv.domain.model.PlayerAnswer
import com.clean.quiztv.domain.model.PlayerStanding
import com.clean.quiztv.domain.model.Question
import com.clean.quiztv.domain.model.QuestionPack
import com.clean.quiztv.domain.model.TEAM_PRESET_PALETTE
import com.clean.quiztv.domain.model.Team
import com.clean.quiztv.domain.model.TeamStanding
import com.clean.quiztv.domain.protocol.ServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

sealed interface GameState {
    val players: List<Player>
    val language: AppLanguage
    val gameFormat: GameFormat

    data class Lobby(
        override val players: List<Player> = emptyList(),
        val availablePacks: List<QuestionPack> = emptyList(),
        val selectedPackId: String? = null,
        val selectedMode: GameMode = GameMode.ADULTS,
        override val gameFormat: GameFormat = GameFormat.INDIVIDUAL,
        override val language: AppLanguage = AppLanguage.RU,
        val step: LobbyStep = LobbyStep.SETUP,
        val teams: List<Team> = emptyList(),
        val roundDurationSeconds: Int = 20
    ) : GameState

    data class QuestionRound(
        override val players: List<Player>,
        val pack: QuestionPack,
        val questionIndex: Int,
        val question: Question,
        val secondsRemaining: Int,
        val answersReceived: Int,
        val totalPlayers: Int,
        override val gameFormat: GameFormat = GameFormat.INDIVIDUAL,
        override val language: AppLanguage = AppLanguage.RU
    ) : GameState

    data class Reveal(
        override val players: List<Player>,
        val pack: QuestionPack,
        val questionIndex: Int,
        val question: Question,
        val optionCounts: Map<Int, Int>,
        val secondsRemaining: Int,
        override val gameFormat: GameFormat = GameFormat.INDIVIDUAL,
        override val language: AppLanguage = AppLanguage.RU
    ) : GameState

    data class Leaderboard(
        override val players: List<Player>,
        val pack: QuestionPack,
        val questionIndex: Int,
        val standings: List<PlayerStanding>,
        val teamStandings: List<TeamStanding> = emptyList(),
        val isLastQuestion: Boolean,
        override val gameFormat: GameFormat = GameFormat.INDIVIDUAL,
        override val language: AppLanguage = AppLanguage.RU
    ) : GameState

    data class Podium(
        override val players: List<Player>,
        val winner: Player?,
        val winningTeam: TeamStanding? = null,
        val standings: List<PlayerStanding>,
        val teamStandings: List<TeamStanding> = emptyList(),
        override val gameFormat: GameFormat = GameFormat.INDIVIDUAL,
        override val language: AppLanguage = AppLanguage.RU
    ) : GameState
}

class GameStateMachine(
    private val repository: IQuestionPackRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val mutex = Mutex()

    var currentLanguage: AppLanguage = AppLanguage.RU
        private set

    var currentFormat: GameFormat = GameFormat.INDIVIDUAL
        private set

    var currentMode: GameMode = GameMode.ADULTS
        private set

    var roundDurationSeconds: Int = 20

    suspend fun setRoundDuration(seconds: Int) = mutex.withLock {
        roundDurationSeconds = seconds
        val current = _state.value
        if (current is GameState.Lobby) {
            _state.value = current.copy(roundDurationSeconds = seconds)
            broadcastLobbyUpdate()
        }
    }

    private val _state = MutableStateFlow<GameState>(GameState.Lobby())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _outgoingMessages = MutableSharedFlow<Pair<String?, ServerMessage>>(extraBufferCapacity = 64)
    val outgoingMessages: SharedFlow<Pair<String?, ServerMessage>> = _outgoingMessages.asSharedFlow()

    private val playersMap = mutableMapOf<String, Player>()
    private val teamsMap = mutableMapOf<String, Team>()
    private var timerJob: Job? = null
    private var transitionJob: Job? = null

    init {
        coroutineScope.launch {
            refreshPacks()
        }
    }

    suspend fun refreshPacks() {
        val packs = repository.getAllPacks()
        mutex.withLock {
            val current = _state.value
            if (current is GameState.Lobby) {
                val defaultPackId = current.selectedPackId ?: packs.firstOrNull { it.ageGroup == current.selectedMode }?.id
                _state.value = current.copy(
                    availablePacks = packs,
                    selectedPackId = defaultPackId,
                    gameFormat = currentFormat,
                    language = currentLanguage,
                    teams = getTeamsList()
                )
                broadcastLobbyUpdate()
            }
        }
    }

    suspend fun selectFormat(format: GameFormat) = mutex.withLock {
        currentFormat = format
        if (format == GameFormat.TEAMS && roundDurationSeconds == 20) {
            roundDurationSeconds = 30
        } else if (format == GameFormat.INDIVIDUAL && roundDurationSeconds == 30) {
            roundDurationSeconds = 20
        }
        val current = _state.value
        if (current is GameState.Lobby) {
            _state.value = current.copy(
                gameFormat = format,
                teams = getTeamsList(),
                roundDurationSeconds = roundDurationSeconds
            )
            broadcastLobbyUpdate()
        }
    }

    suspend fun createTeam(playerId: String, rawName: String) = mutex.withLock {
        val trimmed = rawName.trim().take(22).ifEmpty {
            if (currentLanguage == AppLanguage.PL) "Drużyna ${teamsMap.size + 1}" else "Команда ${teamsMap.size + 1}"
        }
        val (color, emoji) = TEAM_PRESET_PALETTE[teamsMap.size % TEAM_PRESET_PALETTE.size]
        val teamId = "team_${UUID.randomUUID().toString().take(6)}"
        val team = Team(
            id = teamId,
            name = trimmed,
            colorHex = color,
            emoji = emoji,
            memberCount = 1
        )
        teamsMap[teamId] = team

        val player = playersMap[playerId]
        if (player != null) {
            playersMap[playerId] = player.copy(teamId = teamId, teamName = trimmed)
        }
        updatePlayersInCurrentState()
        broadcastLobbyUpdate()
    }

    suspend fun selectTeam(playerId: String, teamId: String) = mutex.withLock {
        val player = playersMap[playerId] ?: return@withLock
        val team = teamsMap[teamId] ?: return@withLock
        playersMap[playerId] = player.copy(teamId = teamId, teamName = team.name)
        updatePlayersInCurrentState()
        broadcastLobbyUpdate()
    }

    suspend fun replayCurrentMelody() = mutex.withLock {
        val s = _state.value
        if (s is GameState.QuestionRound) {
            if (s.question.melodyTune != null) {
                TvSoundManager.replayMelody()
            }
        }
    }

    suspend fun setLanguage(lang: AppLanguage) = mutex.withLock {
        currentLanguage = lang
        val current = _state.value
        when (current) {
            is GameState.Lobby -> {
                _state.value = current.copy(language = lang)
                broadcastLobbyUpdate()
            }
            is GameState.QuestionRound -> _state.value = current.copy(language = lang)
            is GameState.Reveal -> _state.value = current.copy(language = lang)
            is GameState.Leaderboard -> _state.value = current.copy(language = lang)
            is GameState.Podium -> _state.value = current.copy(language = lang)
        }
    }

    suspend fun selectMode(mode: GameMode) = mutex.withLock {
        currentMode = mode
        val current = _state.value
        if (current is GameState.Lobby) {
            val matchingPack = current.availablePacks.firstOrNull { it.ageGroup == mode }?.id ?: current.selectedPackId
            _state.value = current.copy(selectedMode = mode, selectedPackId = matchingPack)
            broadcastLobbyUpdate()
        }
    }

    suspend fun selectPack(packId: String) = mutex.withLock {
        val current = _state.value
        if (current is GameState.Lobby) {
            val pack = current.availablePacks.find { it.id == packId }
            val mode = pack?.ageGroup ?: current.selectedMode
            currentMode = mode
            _state.value = current.copy(selectedPackId = packId, selectedMode = mode)
            broadcastLobbyUpdate()
        }
    }

    suspend fun setLobbyStep(step: LobbyStep) = mutex.withLock {
        val current = _state.value
        if (current is GameState.Lobby) {
            _state.value = current.copy(step = step)
            broadcastLobbyUpdate()
        }
    }

    // =========================================================================
    // Player Connection & Lifecycle
    // =========================================================================

    suspend fun handleJoin(
        suggestedPlayerId: String?,
        name: String,
        avatar: String
    ): Player = mutex.withLock {
        val existing = suggestedPlayerId?.let { playersMap[it] }
        val id = existing?.id ?: UUID.randomUUID().toString()

        val player = Player(
            id = id,
            name = name.trim().ifEmpty {
                if (currentLanguage == AppLanguage.PL) "Gracz ${playersMap.size + 1}" else "Игрок ${playersMap.size + 1}"
            },
            avatar = avatar,
            score = existing?.score ?: 0,
            streak = existing?.streak ?: 0,
            isVip = false,
            isConnected = true,
            teamId = existing?.teamId,
            teamName = existing?.teamName
        )
        playersMap[id] = player

        updatePlayersInCurrentState()
        val lobby = _state.value as? GameState.Lobby
        sendToPlayer(
            id,
            ServerMessage.Welcome(
                playerId = id,
                isVip = false,
                serverState = getStateName(_state.value),
                language = currentLanguage,
                selectedMode = lobby?.selectedMode ?: currentMode,
                selectedPackId = lobby?.selectedPackId,
                gameFormat = lobby?.gameFormat ?: currentFormat,
                teamId = player.teamId,
                teamName = player.teamName,
                teams = getTeamsList()
            )
        )
        broadcastLobbyUpdate()
        player
    }

    suspend fun handleReconnect(playerId: String): Boolean = mutex.withLock {
        val existing = playersMap[playerId] ?: return false
        val updated = existing.copy(isConnected = true)
        playersMap[playerId] = updated

        updatePlayersInCurrentState()

        val lobby = _state.value as? GameState.Lobby
        sendToPlayer(
            playerId,
            ServerMessage.Welcome(
                playerId = playerId,
                isVip = false,
                serverState = getStateName(_state.value),
                language = currentLanguage,
                selectedMode = lobby?.selectedMode ?: currentMode,
                selectedPackId = lobby?.selectedPackId,
                gameFormat = lobby?.gameFormat ?: currentFormat,
                teamId = updated.teamId,
                teamName = updated.teamName,
                teams = getTeamsList()
            )
        )
        syncPlayerState(playerId)
        broadcastLobbyUpdate()
        true
    }

    suspend fun handleDisconnect(playerId: String) = mutex.withLock {
        // If disconnecting in Lobby, remove player immediately so ghost cards never linger
        if (_state.value is GameState.Lobby) {
            playersMap.remove(playerId)
            updatePlayersInCurrentState()
            broadcastLobbyUpdate()
            return@withLock
        }

        val existing = playersMap[playerId] ?: return@withLock
        val updated = existing.copy(isConnected = false)
        playersMap[playerId] = updated

        updatePlayersInCurrentState()
        broadcastLobbyUpdate()
    }

    suspend fun handleLeave(playerId: String) = mutex.withLock {
        playersMap.remove(playerId)
        updatePlayersInCurrentState()
        broadcastLobbyUpdate()
    }

    // =========================================================================
    // Game Flow Controls
    // =========================================================================

    suspend fun startGame(packId: String, gameMode: GameMode = GameMode.ADULTS) {
        mutex.withLock {
            val currentLobby = _state.value as? GameState.Lobby
            val format = currentLobby?.gameFormat ?: currentFormat
            currentFormat = format

            // Auto-balance teams in TEAMS mode if players haven't picked a team
            if (format == GameFormat.TEAMS) {
                if (teamsMap.isEmpty()) {
                    val (c1, e1) = TEAM_PRESET_PALETTE[0]
                    val (c2, e2) = TEAM_PRESET_PALETTE[1]
                    val t1 = Team("team_1", if (currentLanguage == AppLanguage.PL) "Drużyna 1" else "Команда 1", c1, e1)
                    val t2 = Team("team_2", if (currentLanguage == AppLanguage.PL) "Drużyna 2" else "Команда 2", c2, e2)
                    teamsMap["team_1"] = t1
                    teamsMap["team_2"] = t2
                }
                val activePlayers = playersMap.values.filter { it.isConnected }
                val teamIds = teamsMap.keys.toList()
                activePlayers.forEachIndexed { idx, p ->
                    if (p.teamId == null || !teamsMap.containsKey(p.teamId)) {
                        val assignedId = teamIds[idx % teamIds.size]
                        val team = teamsMap[assignedId]!!
                        playersMap[p.id] = p.copy(teamId = assignedId, teamName = team.name)
                    }
                }
            }

            // Anti-repetition: fetch pack with fresh, unseen questions
            val rawPack = repository.getPreparedRoundPack(packId)
                ?: repository.getPackById(packId)
                ?: return
            if (rawPack.questions.isEmpty()) return
            currentMode = gameMode
            val pack = rawPack.copy(ageGroup = gameMode)

            // Reset player scores & streaks
            playersMap.keys.forEach { id ->
                val p = playersMap[id]!!
                playersMap[id] = p.copy(score = 0, streak = 0, lastAnswer = null)
            }

            TvSoundManager.play(TvSoundManager.SoundEffect.ROUND_START)
            startQuestionRound(pack, 0)
        }
    }

    private suspend fun startQuestionRound(pack: QuestionPack, index: Int) {
        timerJob?.cancel()
        transitionJob?.cancel()
        TvSoundManager.stopMelody()

        // Clear last round answers
        playersMap.keys.forEach { id ->
            val p = playersMap[id]!!
            playersMap[id] = p.copy(lastAnswer = null)
        }

        val question = pack.questions[index]
        val totalPlayers = playersMap.values.count { it.isConnected }
        val timeLimit = when {
            pack.category.equals("ChGK", ignoreCase = true) && roundDurationSeconds <= 20 -> 60
            roundDurationSeconds > 0 -> roundDurationSeconds
            else -> question.timeLimitSeconds
        }

        _state.value = GameState.QuestionRound(
            players = playersMap.values.toList(),
            pack = pack,
            questionIndex = index,
            question = question,
            secondsRemaining = timeLimit,
            answersReceived = 0,
            totalPlayers = totalPlayers,
            gameFormat = currentFormat,
            language = currentLanguage
        )

        // If question has a synthesized melody, play it on the TV!
        if (question.melodyTune != null) {
            TvSoundManager.playMelody(question.melodyTune)
        }

        // Broadcast QUESTION_START with exact questionId & localized content
        broadcast(
            ServerMessage.QuestionStart(
                questionId = question.id,
                questionIndex = index + 1,
                totalQuestions = pack.questions.size,
                timeLimitSeconds = timeLimit,
                text = question.localizedText(currentLanguage),
                options = question.localizedOptions(currentLanguage),
                gameMode = pack.ageGroup,
                melodyTune = question.melodyTune,
                rebusEmoji = question.rebusEmoji
            )
        )

        // Launch timer countdown
        startRoundTimer(timeLimit)
    }

    private fun startRoundTimer(seconds: Int) {
        timerJob?.cancel()
        timerJob = coroutineScope.launch {
            var sec = seconds
            while (sec > 0) {
                delay(1000L)
                mutex.withLock {
                    val s = _state.value as? GameState.QuestionRound ?: return@launch
                    sec--
                    _state.value = s.copy(secondsRemaining = sec)
                    if (sec in 1..5) {
                        TvSoundManager.play(TvSoundManager.SoundEffect.URGENT_TICK)
                    } else if (sec > 5) {
                        TvSoundManager.play(TvSoundManager.SoundEffect.TICK)
                    }
                    if (sec <= 0) {
                        TvSoundManager.stopMelody()
                        endQuestionRound()
                        return@launch
                    }
                }
            }
        }
    }

    suspend fun submitAnswer(
        playerId: String,
        questionId: String?,
        optionIndex: Int,
        timeElapsedMs: Long
    ) {
        mutex.withLock {
            val s = _state.value
            if (s !is GameState.QuestionRound) return

            // Do not reject if questionId is null or matches question.id or index
            if (!questionId.isNullOrBlank() &&
                questionId != s.question.id &&
                questionId != "q_${s.questionIndex}" &&
                questionId != "q_${s.questionIndex + 1}") {
                return
            }

            val player = playersMap[playerId] ?: return
            if (player.lastAnswer != null) return // Already answered

            val isCorrect = (optionIndex == s.question.correctIndex)
            val effectiveTimeElapsedMs = if (timeElapsedMs > 0 && timeElapsedMs <= s.question.timeLimitSeconds * 1000L) {
                timeElapsedMs
            } else {
                ((s.question.timeLimitSeconds - s.secondsRemaining) * 1000L).coerceAtLeast(0L)
            }
            val points = ScoringEngine.calculatePoints(
                isCorrect = isCorrect,
                timeLimitSeconds = s.question.timeLimitSeconds,
                timeElapsedMs = effectiveTimeElapsedMs,
                currentStreak = player.streak
            )

            val updatedPlayer = player.copy(
                score = player.score + points,
                streak = if (isCorrect) player.streak + 1 else 0,
                lastAnswer = PlayerAnswer(
                    optionIndex = optionIndex,
                    timeElapsedMs = effectiveTimeElapsedMs,
                    isCorrect = isCorrect,
                    pointsEarned = points
                )
            )
            playersMap[playerId] = updatedPlayer

            // Send instant confirmation ACK to the player
            sendToPlayer(playerId, ServerMessage.AnswerAck(selectedIndex = optionIndex))

            val newAnswersCount = s.answersReceived + 1
            val connectedCount = playersMap.values.count { it.isConnected }
            _state.value = s.copy(
                players = playersMap.values.toList(),
                answersReceived = newAnswersCount
            )

            // If all connected players have answered, conclude round early
            if (newAnswersCount >= connectedCount && connectedCount > 0) {
                timerJob?.cancel()
                TvSoundManager.stopMelody()
                endQuestionRound()
            }
        }
    }

    private suspend fun endQuestionRound() {
        val s = _state.value
        if (s !is GameState.QuestionRound) return
        TvSoundManager.stopMelody()

        // Calculate option distribution counts
        val counts = mutableMapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)
        playersMap.values.forEach { p ->
            p.lastAnswer?.let { ans ->
                counts[ans.optionIndex] = (counts[ans.optionIndex] ?: 0) + 1
            }
        }

        // Sound effect on TV: Correct chord or wrong buzz
        val anyCorrect = playersMap.values.any { it.lastAnswer?.isCorrect == true }
        if (anyCorrect) {
            TvSoundManager.play(TvSoundManager.SoundEffect.CORRECT)
        } else {
            TvSoundManager.play(TvSoundManager.SoundEffect.WRONG)
        }

        val standings = computeStandings()

        // 1. Send individual ROUND_RESULT to each player
        playersMap.values.forEach { p ->
            val rank = standings.find { it.playerId == p.id }?.rank ?: 1
            val ans = p.lastAnswer
            val msg = ServerMessage.RoundResult(
                correctIndex = s.question.correctIndex,
                isCorrect = ans?.isCorrect ?: false,
                pointsEarned = ans?.pointsEarned ?: 0,
                totalScore = p.score,
                streak = p.streak,
                rank = rank,
                funFact = s.question.localizedFunFact(currentLanguage)
            )
            sendToPlayer(p.id, msg)
        }

        // 2. Transition TV state to REVEAL
        _state.value = GameState.Reveal(
            players = playersMap.values.toList(),
            pack = s.pack,
            questionIndex = s.questionIndex,
            question = s.question,
            optionCounts = counts,
            secondsRemaining = 5,
            gameFormat = currentFormat,
            language = currentLanguage
        )

        // 3. Auto-progress to Leaderboard after reveal countdown
        transitionJob?.cancel()
        transitionJob = coroutineScope.launch {
            delay(5000L)
            mutex.withLock {
                val current = _state.value
                if (current is GameState.Reveal) {
                    showLeaderboard(current.pack, current.questionIndex)
                }
            }
        }
    }

    private suspend fun showLeaderboard(pack: QuestionPack, questionIndex: Int) {
        val standings = computeStandings()
        val teamStandings = if (currentFormat == GameFormat.TEAMS) computeTeamStandings() else emptyList()
        val isLast = (questionIndex + 1 >= pack.questions.size)

        _state.value = GameState.Leaderboard(
            players = playersMap.values.toList(),
            pack = pack,
            questionIndex = questionIndex,
            standings = standings,
            teamStandings = teamStandings,
            isLastQuestion = isLast,
            gameFormat = currentFormat,
            language = currentLanguage
        )

        broadcast(
            ServerMessage.LeaderboardUpdate(
                standings = standings,
                teamStandings = teamStandings,
                isFinal = isLast
            )
        )

        // Auto-progress to next question or podium
        transitionJob?.cancel()
        transitionJob = coroutineScope.launch {
            delay(5000L)
            mutex.withLock {
                val current = _state.value
                if (current is GameState.Leaderboard) {
                    proceedAfterLeaderboardLocked()
                }
            }
        }
    }

    suspend fun advanceGame() = mutex.withLock {
        when (val s = _state.value) {
            is GameState.QuestionRound -> {
                timerJob?.cancel()
                TvSoundManager.stopMelody()
                endQuestionRound()
            }
            is GameState.Reveal -> {
                transitionJob?.cancel()
                showLeaderboard(s.pack, s.questionIndex)
            }
            is GameState.Leaderboard -> {
                transitionJob?.cancel()
                proceedAfterLeaderboardLocked()
            }
            else -> {}
        }
    }

    suspend fun proceedAfterLeaderboard() = mutex.withLock {
        proceedAfterLeaderboardLocked()
    }

    private suspend fun proceedAfterLeaderboardLocked() {
        transitionJob?.cancel()
        val s = _state.value
        if (s !is GameState.Leaderboard) return

        if (s.isLastQuestion) {
            val standings = computeStandings()
            val teamStandings = if (currentFormat == GameFormat.TEAMS) computeTeamStandings() else emptyList()
            val winner = standings.firstOrNull()?.let { playersMap[it.playerId] }
            val winningTeam = teamStandings.firstOrNull()

            TvSoundManager.play(TvSoundManager.SoundEffect.FANFARE)

            _state.value = GameState.Podium(
                players = playersMap.values.toList(),
                winner = winner,
                winningTeam = winningTeam,
                standings = standings,
                teamStandings = teamStandings,
                gameFormat = currentFormat,
                language = currentLanguage
            )
            standings.forEach { standing ->
                sendToPlayer(
                    standing.playerId,
                    ServerMessage.GameOver(
                        finalRank = standing.rank,
                        finalScore = standing.score,
                        winnerName = winner?.name ?: if (currentLanguage == AppLanguage.PL) "Mistrz" else "Победитель",
                        winningTeam = winningTeam?.name
                    )
                )
            }
        } else {
            startQuestionRound(s.pack, s.questionIndex + 1)
        }
    }

    suspend fun resetToLobby() {
        val packs = repository.getAllPacks()
        mutex.withLock {
            timerJob?.cancel()
            transitionJob?.cancel()
            TvSoundManager.stopMelody()

            playersMap.keys.forEach { id ->
                val p = playersMap[id]!!
                playersMap[id] = p.copy(score = 0, streak = 0, lastAnswer = null)
            }
            val lobby = _state.value as? GameState.Lobby
            val mode = lobby?.selectedMode ?: currentMode
            _state.value = GameState.Lobby(
                players = playersMap.values.filter { it.isConnected },
                availablePacks = packs,
                selectedPackId = packs.firstOrNull { it.ageGroup == mode }?.id,
                selectedMode = mode,
                gameFormat = currentFormat,
                language = currentLanguage,
                step = LobbyStep.THEME_SELECT,
                teams = getTeamsList()
            )
            broadcastLobbyUpdate()
        }
    }

    // =========================================================================
    // Helper Sync & Messaging
    // =========================================================================

    private fun getTeamsList(): List<Team> {
        return teamsMap.values.map { t ->
            val count = playersMap.values.count { it.isConnected && it.teamId == t.id }
            t.copy(memberCount = count)
        }
    }

    private fun computeStandings(): List<PlayerStanding> {
        val sorted = playersMap.values.sortedByDescending { it.score }
        return sorted.mapIndexed { idx, player ->
            PlayerStanding(
                rank = idx + 1,
                playerId = player.id,
                name = player.name,
                avatar = player.avatar,
                score = player.score,
                streak = player.streak,
                lastScoreDiff = player.lastAnswer?.pointsEarned ?: 0,
                teamId = player.teamId,
                teamName = player.teamName
            )
        }
    }

    private fun computeTeamStandings(): List<TeamStanding> {
        val teams = playersMap.values.filter { it.teamId != null }
            .groupBy { it.teamId!! }

        return teams.map { (teamId, teamPlayers) ->
            val team = teamsMap[teamId]
            TeamStanding(
                rank = 1,
                teamId = teamId,
                name = team?.name ?: (teamPlayers.firstOrNull()?.teamName ?: "Команда"),
                colorHex = team?.colorHex ?: "#3B82F6",
                emoji = team?.emoji ?: "01",
                totalScore = teamPlayers.sumOf { it.score },
                playersCount = teamPlayers.size
            )
        }.sortedByDescending { it.totalScore }
        .mapIndexed { idx, ts -> ts.copy(rank = idx + 1) }
    }

    suspend fun syncPlayerState(playerId: String) {
        val p = playersMap[playerId] ?: return
        when (val s = _state.value) {
            is GameState.Lobby -> {
                broadcastLobbyUpdate()
            }
            is GameState.QuestionRound -> {
                sendToPlayer(
                    playerId,
                    ServerMessage.SyncState(
                        state = "QUESTION",
                        score = p.score,
                        alreadyAnswered = p.lastAnswer != null,
                        selectedIndex = p.lastAnswer?.optionIndex,
                        questionIndex = s.questionIndex + 1,
                        totalQuestions = s.pack.questions.size,
                        timeRemainingSeconds = s.secondsRemaining,
                        options = s.question.localizedOptions(currentLanguage),
                        questionText = s.question.localizedText(currentLanguage),
                        questionId = s.question.id,
                        gameMode = currentMode
                    )
                )
            }
            is GameState.Reveal -> {
                sendToPlayer(
                    playerId,
                    ServerMessage.SyncState(
                        state = "REVEAL",
                        score = p.score,
                        alreadyAnswered = true,
                        selectedIndex = p.lastAnswer?.optionIndex,
                        questionIndex = s.questionIndex + 1,
                        totalQuestions = s.pack.questions.size,
                        timeRemainingSeconds = s.secondsRemaining,
                        questionId = s.question.id,
                        gameMode = currentMode
                    )
                )
            }
            is GameState.Leaderboard -> {
                sendToPlayer(
                    playerId,
                    ServerMessage.SyncState(
                        state = "LEADERBOARD",
                        score = p.score,
                        alreadyAnswered = true,
                        questionIndex = s.questionIndex + 1,
                        totalQuestions = s.pack.questions.size,
                        questionId = s.pack.questions.getOrNull(s.questionIndex)?.id,
                        gameMode = currentMode
                    )
                )
                sendToPlayer(
                    playerId,
                    ServerMessage.LeaderboardUpdate(
                        standings = s.standings,
                        teamStandings = s.teamStandings,
                        isFinal = s.isLastQuestion
                    )
                )
            }
            is GameState.Podium -> {
                sendToPlayer(
                    playerId,
                    ServerMessage.SyncState(
                        state = "PODIUM",
                        score = p.score,
                        alreadyAnswered = true,
                        gameMode = currentMode
                    )
                )
                val standing = s.standings.find { it.playerId == playerId }
                sendToPlayer(
                    playerId,
                    ServerMessage.GameOver(
                        finalRank = standing?.rank ?: 1,
                        finalScore = standing?.score ?: p.score,
                        winnerName = s.winner?.name ?: if (currentLanguage == AppLanguage.PL) "Mistrz" else "Победитель",
                        winningTeam = s.winningTeam?.name
                    )
                )
            }
        }
    }

    private suspend fun broadcastLobbyUpdate() {
        val s = _state.value
        val lobby = s as? GameState.Lobby
        val packList = (lobby?.availablePacks ?: emptyList()).map { pack ->
            ServerMessage.LobbyUpdate.PackInfo(
                id = pack.id,
                title = pack.localizedTitle(currentLanguage),
                description = pack.localizedDescription(currentLanguage),
                ageGroup = pack.ageGroup,
                questionsCount = pack.questions.size,
                accentColor = pack.accentColor
            )
        }

        val playerList = playersMap.values.filter { it.isConnected }.map {
            ServerMessage.LobbyUpdate.PlayerInfo(
                id = it.id,
                name = it.name,
                avatar = it.avatar,
                isVip = it.isVip,
                teamId = it.teamId,
                teamName = it.teamName
            )
        }

        broadcast(
            ServerMessage.LobbyUpdate(
                playersCount = playerList.size,
                players = playerList,
                availablePacks = packList,
                selectedMode = lobby?.selectedMode ?: currentMode,
                selectedPackId = lobby?.selectedPackId,
                language = currentLanguage,
                gameFormat = lobby?.gameFormat ?: currentFormat,
                step = lobby?.step?.name ?: "SETUP",
                teams = getTeamsList()
            )
        )
    }

    private fun updatePlayersInCurrentState() {
        val list = if (_state.value is GameState.Lobby) {
            playersMap.values.filter { it.isConnected }
        } else {
            playersMap.values.toList()
        }
        val teams = getTeamsList()
        when (val s = _state.value) {
            is GameState.Lobby -> _state.value = s.copy(players = list, teams = teams)
            is GameState.QuestionRound -> _state.value = s.copy(players = list)
            is GameState.Reveal -> _state.value = s.copy(players = list)
            is GameState.Leaderboard -> _state.value = s.copy(players = list)
            is GameState.Podium -> _state.value = s.copy(players = list)
        }
    }

    private suspend fun sendToPlayer(playerId: String, message: ServerMessage) {
        _outgoingMessages.emit(playerId to message)
    }

    private suspend fun broadcast(message: ServerMessage) {
        _outgoingMessages.emit(null to message)
    }

    private fun getStateName(state: GameState): String = when (state) {
        is GameState.Lobby -> "LOBBY"
        is GameState.QuestionRound -> "QUESTION"
        is GameState.Reveal -> "REVEAL"
        is GameState.Leaderboard -> "LEADERBOARD"
        is GameState.Podium -> "PODIUM"
    }
}
