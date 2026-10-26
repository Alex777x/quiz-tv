package com.clean.quiztv.data

import android.content.Context
import android.content.SharedPreferences
import com.clean.quiztv.domain.model.Question

/**
 * Tracks asked questions to guarantee players get fresh questions
 * across games and prevent repetition.
 */
class QuestionHistoryManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("quiz_question_history", Context.MODE_PRIVATE)

    private val keySeenQuestions = "seen_question_ids"

    fun getSeenQuestionIds(): Set<String> {
        return prefs.getStringSet(keySeenQuestions, emptySet()) ?: emptySet()
    }

    fun markQuestionSeen(questionId: String) {
        val current = getSeenQuestionIds().toMutableSet()
        current.add(questionId)
        prefs.edit().putStringSet(keySeenQuestions, current).apply()
    }

    fun markQuestionsSeen(questionIds: Collection<String>) {
        val current = getSeenQuestionIds().toMutableSet()
        current.addAll(questionIds)
        prefs.edit().putStringSet(keySeenQuestions, current).apply()
    }

    /**
     * Filters questions to prioritize unseen ones.
     * If all questions in the pack have already been played, it recycles them
     * by clearing their history, ensuring the game never runs out of content.
     */
    fun selectFreshQuestions(questions: List<Question>, count: Int = 10): List<Question> {
        if (questions.isEmpty()) return emptyList()

        val seen = getSeenQuestionIds()
        val unseen = questions.filterNot { it.id in seen }

        return if (unseen.size >= count) {
            unseen.shuffled().take(count)
        } else if (unseen.isNotEmpty()) {
            // Take all unseen, and fill the rest from recycled seen questions
            val recycled = questions.filter { it.id in seen }.shuffled()
            (unseen + recycled).take(count)
        } else {
            // All questions in this pack were seen! Recycle the pack
            val idsToRemove = questions.map { it.id }.toSet()
            val remaining = seen - idsToRemove
            prefs.edit().putStringSet(keySeenQuestions, remaining).apply()
            questions.shuffled().take(count)
        }
    }

    fun resetAllHistory() {
        prefs.edit().remove(keySeenQuestions).apply()
    }
}
