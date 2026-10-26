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
    fun testVipElectionAndLeaveRoom() = runTest(testDispatcher) {
        val repo = FakeQuestionPackRepository(listOf(samplePack))
        val sm = GameStateMachine(repo, coroutineScope = testScope)
        testScope.testScheduler.runCurrent()

        // 1. First player joins -> becomes VIP
        val p1 = sm.handleJoin(suggestedPlayerId = null, name = "Player 1", avatar = "🦊")
        testScope.testScheduler.runCurrent()
        assertTrue("First player must be VIP", p1.isVip)

        // 2. Second player joins -> not VIP
        val p2 = sm.handleJoin(suggestedPlayerId = null, name = "Player 2", avatar = "🐻")
        testScope.testScheduler.runCurrent()
        assertFalse("Second player should not be VIP", p2.isVip)

        val lobby1 = sm.state.value as GameState.Lobby
        assertEquals(2, lobby1.players.size)

        // 3. Player 1 leaves the room -> Player 2 must automatically become VIP!
        sm.handleLeave(p1.id)
        testScope.testScheduler.runCurrent()

        val lobby2 = sm.state.value as GameState.Lobby
        assertEquals(1, lobby2.players.size)
        val remainingPlayer = lobby2.players.first()
        assertEquals(p2.id, remainingPlayer.id)
        assertTrue("Player 2 must be elected VIP after VIP leaves", remainingPlayer.isVip)
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
    fun testHostPauseAndForceReveal() = runTest(testDispatcher) {
        val repo = FakeQuestionPackRepository(listOf(samplePack))
        val sm = GameStateMachine(repository = repo, coroutineScope = testScope)
        testScope.testScheduler.runCurrent()

        sm.handleJoin(suggestedPlayerId = null, name = "Host", avatar = "👑")
        testScope.testScheduler.runCurrent()

        sm.startGame("test_pack", GameMode.ADULTS)
        testScope.testScheduler.runCurrent()

        assertTrue(sm.state.value is GameState.QuestionRound)
        assertFalse(sm.isPaused)

        // Toggle pause
        sm.togglePause()
        testScope.testScheduler.runCurrent()

        assertTrue(sm.isPaused)
        val roundPaused = sm.state.value as GameState.QuestionRound
        assertTrue(roundPaused.isPaused)

        // Resume pause
        sm.togglePause()
        testScope.testScheduler.runCurrent()
        assertFalse(sm.isPaused)

        // Force reveal
        sm.forceReveal()
        testScope.testScheduler.runCurrent()

        assertTrue(sm.state.value is GameState.Reveal)
    }
}
