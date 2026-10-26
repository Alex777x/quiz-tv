package com.clean.quiztv.domain.protocol

import com.clean.quiztv.domain.model.AppLanguage
import com.clean.quiztv.domain.model.GameFormat
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.PlayerStanding
import com.clean.quiztv.domain.model.Team
import com.clean.quiztv.domain.model.TeamStanding
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface ClientMessage {
    @Serializable
    @SerialName("JOIN")
    data class Join(
        val playerId: String? = null,
        val name: String,
        val avatar: String
    ) : ClientMessage

    @Serializable
    @SerialName("RECONNECT")
    data class Reconnect(
        val playerId: String
    ) : ClientMessage

    @Serializable
    @SerialName("SET_LANGUAGE")
    data class SetLanguage(
        val language: AppLanguage
    ) : ClientMessage

    @Serializable
    @SerialName("SELECT_MODE")
    data class SelectMode(
        val mode: GameMode
    ) : ClientMessage

    @Serializable
    @SerialName("SELECT_PACK")
    data class SelectPack(
        val packId: String
    ) : ClientMessage

    @Serializable
    @SerialName("SELECT_FORMAT")
    data class SelectFormat(
        val format: GameFormat
    ) : ClientMessage

    @Serializable
    @SerialName("CREATE_TEAM")
    data class CreateTeam(
        val name: String
    ) : ClientMessage

    @Serializable
    @SerialName("SELECT_TEAM")
    data class SelectTeam(
        val teamId: String
    ) : ClientMessage

    @Serializable
    @SerialName("GO_TO_STEP")
    data class GoToStep(
        val step: String
    ) : ClientMessage

    @Serializable
    @SerialName("LEAVE_ROOM")
    data object LeaveRoom : ClientMessage

    @Serializable
    @SerialName("START_GAME")
    data class StartGame(
        val packId: String,
        val gameMode: GameMode = GameMode.ADULTS
    ) : ClientMessage

    @Serializable
    @SerialName("SUBMIT_ANSWER")
    data class SubmitAnswer(
        val questionId: String? = null,
        val optionIndex: Int,
        val timeElapsedMs: Long = 0L
    ) : ClientMessage

    @Serializable
    @SerialName("TOGGLE_PAUSE")
    data object TogglePause : ClientMessage

    @Serializable
    @SerialName("FORCE_REVEAL")
    data object ForceReveal : ClientMessage

    @Serializable
    @SerialName("REPLAY_MELODY")
    data object ReplayMelody : ClientMessage

    @Serializable
    @SerialName("RETURN_TO_LOBBY")
    data object ReturnToLobby : ClientMessage

    @Serializable
    @SerialName("NEXT_QUESTION")
    data object NextQuestion : ClientMessage

    @Serializable
    @SerialName("PLAY_AGAIN")
    data object PlayAgain : ClientMessage
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed interface ServerMessage {
    @Serializable
    @SerialName("WELCOME")
    data class Welcome(
        val playerId: String,
        val isVip: Boolean,
        val serverState: String,
        val language: AppLanguage = AppLanguage.RU,
        val selectedMode: GameMode = GameMode.ADULTS,
        val selectedPackId: String? = null,
        val gameFormat: GameFormat = GameFormat.INDIVIDUAL,
        val teamId: String? = null,
        val teamName: String? = null,
        val teams: List<Team> = emptyList()
    ) : ServerMessage

    @Serializable
    @SerialName("LOBBY_UPDATE")
    data class LobbyUpdate(
        val playersCount: Int,
        val players: List<PlayerInfo>,
        val availablePacks: List<PackInfo>,
        val selectedMode: GameMode = GameMode.ADULTS,
        val selectedPackId: String? = null,
        val language: AppLanguage = AppLanguage.RU,
        val gameFormat: GameFormat = GameFormat.INDIVIDUAL,
        val step: String = "SETUP",
        val teams: List<Team> = emptyList()
    ) : ServerMessage {
        @Serializable
        data class PlayerInfo(
            val id: String,
            val name: String,
            val avatar: String,
            val isVip: Boolean,
            val teamId: String? = null,
            val teamName: String? = null
        )

        @Serializable
        data class PackInfo(
            val id: String,
            val title: String,
            val description: String,
            val ageGroup: GameMode,
            val questionsCount: Int,
            val accentColor: String
        )
    }

    @Serializable
    @SerialName("QUESTION_START")
    data class QuestionStart(
        val questionId: String,
        val questionIndex: Int,
        val totalQuestions: Int,
        val timeLimitSeconds: Int,
        val text: String,
        val options: List<String>,
        val gameMode: GameMode,
        val melodyTune: String? = null,
        val rebusEmoji: String? = null
    ) : ServerMessage

    @Serializable
    @SerialName("ANSWER_ACK")
    data class AnswerAck(
        val selectedIndex: Int
    ) : ServerMessage

    @Serializable
    @SerialName("ROUND_RESULT")
    data class RoundResult(
        val correctIndex: Int,
        val isCorrect: Boolean,
        val pointsEarned: Int,
        val totalScore: Int,
        val streak: Int,
        val rank: Int,
        val funFact: String
    ) : ServerMessage

    @Serializable
    @SerialName("LEADERBOARD_UPDATE")
    data class LeaderboardUpdate(
        val standings: List<PlayerStanding>,
        val teamStandings: List<TeamStanding> = emptyList(),
        val isFinal: Boolean = false
    ) : ServerMessage

    @Serializable
    @SerialName("GAME_OVER")
    data class GameOver(
        val finalRank: Int,
        val finalScore: Int,
        val winnerName: String,
        val winningTeam: String? = null
    ) : ServerMessage

    @Serializable
    @SerialName("SYNC_STATE")
    data class SyncState(
        val state: String,
        val score: Int,
        val alreadyAnswered: Boolean,
        val selectedIndex: Int? = null,
        val questionIndex: Int = 0,
        val totalQuestions: Int = 0,
        val timeRemainingSeconds: Int = 0,
        val isPaused: Boolean = false,
        val options: List<String> = emptyList(),
        val questionText: String? = null,
        val questionId: String? = null,
        val gameMode: GameMode = GameMode.ADULTS
    ) : ServerMessage

    @Serializable
    @SerialName("GAME_PAUSED")
    data class GamePaused(
        val isPaused: Boolean
    ) : ServerMessage

    @Serializable
    @SerialName("ERROR")
    data class Error(
        val message: String
    ) : ServerMessage
}
