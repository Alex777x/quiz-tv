package com.clean.quiztv

import com.clean.quiztv.data.IQuestionPackRepository
import com.clean.quiztv.domain.model.AppLanguage
import com.clean.quiztv.domain.model.GameFormat
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.Question
import com.clean.quiztv.domain.model.QuestionPack
import com.clean.quiztv.domain.model.Team
import com.clean.quiztv.game.GameState
import com.clean.quiztv.game.GameStateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeQuestionPackRepository(
    private val packs: List<QuestionPack>
) : IQuestionPackRepository {
    override suspend fun getAllPacks(): List<QuestionPack> = packs
    override suspend fun getPackById(id: String): QuestionPack? = packs.find { it.id == id }
    override suspend fun getPreparedRoundPack(packId: String, count: Int): QuestionPack? = packs.find { it.id == packId }
    override suspend fun savePack(pack: QuestionPack): Boolean = true
}

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachineTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val samplePack = QuestionPack(
        id = "test_pack",
        title = "Тестовый пак",
        titlePl = "Testowa paczka",
        ageGroup = GameMode.ADULTS,
        questions = listOf(
            Question(
                id = "q_real_1",
                text = "Вопрос 1",
                textPl = "Pytanie 1",
                options = listOf("A", "B", "C", "D"),
                optionsPl = listOf("A", "B", "C", "D"),
                correctIndex = 1, // Option B is correct
                timeLimitSeconds = 30,
                funFact = "Факт 1"
            )
        )
    )

    @Test
    fun testCorrectAnswerEvaluatesToTrueAndAwardsPoints() = runTest(testDispatcher) {
        val repo = FakeQuestionPackRepository(listOf(samplePack))

        val sm = GameStateMachine(repo, coroutineScope = testScope)
        testScope.testScheduler.runCurrent()

        // 1. Join player
        val player = sm.handleJoin(suggestedPlayerId = null, name = "Tester", avatar = "🦊")
        testScope.testScheduler.runCurrent()

        // 2. Start game
        sm.startGame("test_pack", GameMode.ADULTS)
        testScope.testScheduler.runCurrent()

        assertTrue("State should be QuestionRound before answering", sm.state.value is GameState.QuestionRound)

        // 3. Submit correct answer with synthetic questionId or real ID
        sm.submitAnswer(
            playerId = player.id,
            questionId = "q_real_1",
            optionIndex = 1, // Correct
            timeElapsedMs = 1500L
        )
        testScope.testScheduler.runCurrent()

        // Since all connected players (1 of 1) answered, the round ends immediately to Reveal
        assertTrue("State should transition to Reveal", sm.state.value is GameState.Reveal)

        val revealState = sm.state.value as GameState.Reveal
        val updatedPlayer = revealState.players.find { it.id == player.id }
        assertNotNull(updatedPlayer)
        assertNotNull(updatedPlayer?.lastAnswer)
        assertTrue(updatedPlayer!!.lastAnswer!!.isCorrect)
        assertTrue(updatedPlayer.score > 100) // Base 100 + speed
        assertEquals(1, updatedPlayer.streak)
    }

    @Test
    fun testIncorrectAnswerEvaluatesToFalseAndAwardsZeroPoints() = runTest(testDispatcher) {
        val repo = FakeQuestionPackRepository(listOf(samplePack))

        val sm = GameStateMachine(repo, coroutineScope = testScope)
        testScope.testScheduler.runCurrent()

        val player = sm.handleJoin(suggestedPlayerId = null, name = "Tester", avatar = "🐻")
        testScope.testScheduler.runCurrent()

        sm.startGame("test_pack", GameMode.ADULTS)
        testScope.testScheduler.runCurrent()

        assertTrue("State should be QuestionRound before answering", sm.state.value is GameState.QuestionRound)

        // Submit WRONG answer (index 0 instead of 1)
        sm.submitAnswer(
            playerId = player.id,
            questionId = "q_real_1",
            optionIndex = 0, // Wrong
            timeElapsedMs = 2000L
        )
        testScope.testScheduler.runCurrent()

        // Since all connected players (1 of 1) answered, round transitions to Reveal
        assertTrue("State should transition to Reveal", sm.state.value is GameState.Reveal)

        val revealState = sm.state.value as GameState.Reveal
        val updatedPlayer = revealState.players.find { it.id == player.id }
        assertNotNull(updatedPlayer?.lastAnswer)
        assertFalse(updatedPlayer!!.lastAnswer!!.isCorrect)
        assertEquals(0, updatedPlayer.score)
        assertEquals(0, updatedPlayer.streak)
    }

    @Test
    fun testLanguageToggleUpdatesLobbyState() = runTest(testDispatcher) {
        val repo = FakeQuestionPackRepository(listOf(samplePack))

        val sm = GameStateMachine(repo, coroutineScope = testScope)
        testScope.testScheduler.runCurrent()

        assertEquals(AppLanguage.RU, sm.currentLanguage)

        sm.setLanguage(AppLanguage.PL)
        testScope.testScheduler.runCurrent()

        assertEquals(AppLanguage.PL, sm.currentLanguage)
        val lobby = sm.state.value as GameState.Lobby
        assertEquals(AppLanguage.PL, lobby.language)
    }

    @Test
    fun testPlayersJoinAndLeaveRoom() = runTest(testDispatcher) {
        val repo = FakeQuestionPackRepository(listOf(samplePack))
        val sm = GameStateMachine(repo, coroutineScope = testScope)
        testScope.testScheduler.runCurrent()

        // 1. First player joins
        val p1 = sm.handleJoin(suggestedPlayerId = null, name = "Player 1", avatar = "🦊")
        testScope.testScheduler.runCurrent()
        assertFalse("Players play on equal terms without VIP privilege", p1.isVip)

        // 2. Second player joins
        val p2 = sm.handleJoin(suggestedPlayerId = null, name = "Player 2", avatar = "🐻")
        testScope.testScheduler.runCurrent()
        assertFalse("Second player is equal participant", p2.isVip)

        val lobby1 = sm.state.value as GameState.Lobby
        assertEquals(2, lobby1.players.size)

        // 3. Player 1 leaves the room -> Player 2 remains
        sm.handleLeave(p1.id)
        testScope.testScheduler.runCurrent()

        val lobby2 = sm.state.value as GameState.Lobby
        assertEquals(1, lobby2.players.size)
        val remainingPlayer = lobby2.players.first()
        assertEquals(p2.id, remainingPlayer.id)
    }

    @Test
    fun testTeamFormatAndSelection() = runTest(testDispatcher) {
        val repo = FakeQuestionPackRepository(listOf(samplePack))
        val sm = GameStateMachine(repo, coroutineScope = testScope)
        testScope.testScheduler.runCurrent()

        val p1 = sm.handleJoin(suggestedPlayerId = null, name = "Anna", avatar = "🦊")
        val p2 = sm.handleJoin(suggestedPlayerId = null, name = "Piotr", avatar = "🐻")
        testScope.testScheduler.runCurrent()

        // Switch format to TEAMS
        sm.selectFormat(GameFormat.TEAMS)
        testScope.testScheduler.runCurrent()

        val lobby = sm.state.value as GameState.Lobby
        assertEquals(GameFormat.TEAMS, lobby.gameFormat)

        // Player 1 creates team "Dream Team"
        sm.createTeam(p1.id, "Dream Team")
        testScope.testScheduler.runCurrent()

        val lobbyAfterTeam = sm.state.value as GameState.Lobby
        assertEquals(1, lobbyAfterTeam.teams.size)
        val team1 = lobbyAfterTeam.teams.first()
        assertEquals("Dream Team", team1.name)

        // Player 2 joins "Dream Team"
        sm.selectTeam(p2.id, team1.id)
        testScope.testScheduler.runCurrent()

        val updatedLobby = sm.state.value as GameState.Lobby
        val updatedP1 = updatedLobby.players.find { it.id == p1.id }
        val updatedP2 = updatedLobby.players.find { it.id == p2.id }
        assertEquals(team1.id, updatedP1?.teamId)
        assertEquals(team1.id, updatedP2?.teamId)
        assertEquals("Dream Team", updatedP1?.teamName)
        assertEquals("Dream Team", updatedP2?.teamName)
    }

    @Test
    fun testRoundAdvancesToRevealWhenAllPlayersAnswer() = runTest(testDispatcher) {
        val repo = FakeQuestionPackRepository(listOf(samplePack))
        val sm = GameStateMachine(repository = repo, coroutineScope = testScope)
        testScope.testScheduler.runCurrent()

        val p = sm.handleJoin(suggestedPlayerId = null, name = "Gamer", avatar = "🎮")
        testScope.testScheduler.runCurrent()

        sm.startGame("test_pack", GameMode.ADULTS)
        testScope.testScheduler.runCurrent()

        assertTrue(sm.state.value is GameState.QuestionRound)

        // Player submits answer -> all connected players have answered -> round automatically reveals!
        sm.submitAnswer(playerId = p.id, questionId = "q_real_1", optionIndex = 1, timeElapsedMs = 3000L)
        testScope.testScheduler.runCurrent()

        assertTrue("Round must transition to Reveal once all players submit answers", sm.state.value is GameState.Reveal)
    }

    @Test
    fun testTeamPointsRetainedWhenPlayerDisconnects() = runTest(testDispatcher) {
        val repo = FakeQuestionPackRepository(listOf(samplePack))
        val sm = GameStateMachine(repository = repo, coroutineScope = testScope)
        testScope.testScheduler.runCurrent()

        // 1. Set format to TEAMS
        sm.selectFormat(GameFormat.TEAMS)
        testScope.testScheduler.runCurrent()

        // 2. Join 2 players and create team
        val p1 = sm.handleJoin(suggestedPlayerId = null, name = "Player 1", avatar = "🦁")
        val p2 = sm.handleJoin(suggestedPlayerId = null, name = "Player 2", avatar = "🦊")
        testScope.testScheduler.runCurrent()

        sm.createTeam(p1.id, "Winners")
        testScope.testScheduler.runCurrent()

        val lobbyState = sm.state.value as GameState.Lobby
        val team = lobbyState.teams.first()

        // Both join "Winners" team
        sm.selectTeam(p1.id, team.id)
        sm.selectTeam(p2.id, team.id)
        testScope.testScheduler.runCurrent()

        // 3. Start game
        sm.startGame("test_pack", GameMode.ADULTS)
        testScope.testScheduler.runCurrent()

        // 4. Both answer correctly
        sm.submitAnswer(p1.id, "q_real_1", 1, 1000L)
        sm.submitAnswer(p2.id, "q_real_1", 1, 1000L)
        testScope.testScheduler.runCurrent()

        assertTrue(sm.state.value is GameState.Reveal)
        val reveal = sm.state.value as GameState.Reveal
        val p1Score = reveal.players.find { it.id == p1.id }!!.score
        val p2Score = reveal.players.find { it.id == p2.id }!!.score
        val combinedScore = p1Score + p2Score
        assertTrue(combinedScore > 200)

        // 5. Player 2 disconnects during the game
        sm.handleDisconnect(p2.id)
        testScope.testScheduler.runCurrent()

        // 6. Advance to Leaderboard
        sm.advanceGame()
        testScope.testScheduler.runCurrent()

        assertTrue(sm.state.value is GameState.Leaderboard)
        val leaderboard = sm.state.value as GameState.Leaderboard
        val winningTeamStanding = leaderboard.teamStandings.find { it.teamId == team.id }

        assertNotNull("Team standings must exist", winningTeamStanding)
        assertEquals(
            "Disconnected player's score must still be counted in team standings",
            combinedScore,
            winningTeamStanding!!.totalScore
        )
    }

    @Test
    fun testKidsModePreservedAfterResetToLobby() = runTest(testDispatcher) {
        val kidsPack = QuestionPack(
            id = "kids_pack_1",
            title = "Детский пак",
            titlePl = "Dziecięca paczka",
            ageGroup = GameMode.KIDS,
            questions = listOf(
                Question(
                    id = "k_q1",
                    text = "Детский вопрос?",
                    textPl = "Dziecięce pytanie?",
                    options = listOf("1", "2", "3", "4"),
                    optionsPl = listOf("1", "2", "3", "4"),
                    correctIndex = 0,
                    timeLimitSeconds = 25,
                    funFact = "Факт",
                    funFactPl = "Fakt"
                )
            )
        )
        val repo = FakeQuestionPackRepository(listOf(samplePack, kidsPack))
        val sm = GameStateMachine(repository = repo, coroutineScope = testScope)
        testScope.testScheduler.runCurrent()

        sm.handleJoin(suggestedPlayerId = null, name = "Child", avatar = "🦄")
        testScope.testScheduler.runCurrent()

        // Select KIDS mode
        sm.selectMode(GameMode.KIDS)
        testScope.testScheduler.runCurrent()

        assertEquals(GameMode.KIDS, sm.currentMode)
        val lobbyKids = sm.state.value as GameState.Lobby
        assertEquals(GameMode.KIDS, lobbyKids.selectedMode)
        assertEquals("kids_pack_1", lobbyKids.selectedPackId)

        // Start game in KIDS mode
        sm.startGame("kids_pack_1", GameMode.KIDS)
        testScope.testScheduler.runCurrent()

        assertEquals(GameMode.KIDS, sm.currentMode)
        assertTrue(sm.state.value is GameState.QuestionRound)

        // Reset to lobby (equivalent to pressing "Play again" at the end of the game)
        sm.resetToLobby()
        testScope.testScheduler.runCurrent()

        assertEquals(GameMode.KIDS, sm.currentMode)
        val resetLobby = sm.state.value as GameState.Lobby
        assertEquals("Lobby must remain in KIDS mode after resetToLobby", GameMode.KIDS, resetLobby.selectedMode)
        assertEquals("kids_pack_1", resetLobby.selectedPackId)
    }
}
