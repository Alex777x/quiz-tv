package com.clean.quiztv

import com.clean.quiztv.game.ScoringEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringEngineTest {

    @Test
    fun testIncorrectAnswerYieldsZero() {
        val points = ScoringEngine.calculatePoints(
            isCorrect = false,
            timeLimitSeconds = 20,
            timeElapsedMs = 1000L,
            currentStreak = 5
        )
        assertEquals(0, points)
    }

    @Test
    fun testInstantAnswerWithMaxStreakYields300() {
        val points = ScoringEngine.calculatePoints(
            isCorrect = true,
            timeLimitSeconds = 20,
            timeElapsedMs = 0L,
            currentStreak = 4 // 4 * 25 = 100 max streak
        )
        // 100 base + 100 speed + 100 streak = 300
        assertEquals(300, points)
    }

    @Test
    fun testMidAnswerNoStreak() {
        val points = ScoringEngine.calculatePoints(
            isCorrect = true,
            timeLimitSeconds = 20,
            timeElapsedMs = 10_000L, // half time
            currentStreak = 0
        )
        // 100 base + 50 speed + 0 streak = 150
        assertEquals(150, points)
    }

    @Test
    fun testLastSecondAnswerHasZeroSpeedBonus() {
        val points = ScoringEngine.calculatePoints(
            isCorrect = true,
            timeLimitSeconds = 20,
            timeElapsedMs = 20_000L,
            currentStreak = 1
        )
        // 100 base + 0 speed + 25 streak = 125
        assertEquals(125, points)
    }
}
