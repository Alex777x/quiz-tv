package com.clean.quiztv.game

import kotlin.math.floor

object ScoringEngine {

    const val BASE_CORRECT_POINTS = 100
    const val MAX_SPEED_BONUS = 100
    const val STREAK_MULTIPLIER = 25
    const val MAX_STREAK_BONUS = 100

    /**
     * Calculates points earned for an answer.
     *
     * @param isCorrect whether the selected option is correct
     * @param timeLimitSeconds total duration of question in seconds
     * @param timeElapsedMs time taken to respond in milliseconds
     * @param currentStreak consecutive correct answers before this question
     * @return points earned (0..300)
     */
    fun calculatePoints(
        isCorrect: Boolean,
        timeLimitSeconds: Int,
        timeElapsedMs: Long,
        currentStreak: Int
    ): Int {
        if (!isCorrect) return 0

        val totalMs = (timeLimitSeconds * 1000L).coerceAtLeast(1000L)
        val remainingMs = (totalMs - timeElapsedMs).coerceIn(0L, totalMs)

        val speedRatio = remainingMs.toDouble() / totalMs.toDouble()
        val speedBonus = floor(MAX_SPEED_BONUS * speedRatio).toInt()

        val streakBonus = (currentStreak * STREAK_MULTIPLIER).coerceAtMost(MAX_STREAK_BONUS)

        return BASE_CORRECT_POINTS + speedBonus + streakBonus
    }
}
