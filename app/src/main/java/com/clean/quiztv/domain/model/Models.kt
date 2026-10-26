package com.clean.quiztv.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AppLanguage {
    RU,
    PL
}

@Serializable
enum class GameMode {
    KIDS,
    ADULTS
}

@Serializable
enum class GameFormat {
    INDIVIDUAL,
    TEAMS
}

@Serializable
enum class LobbyStep {
    SETUP,
    CONNECT,
    THEME_SELECT
}

@Serializable
data class Team(
    val id: String,
    val name: String,
    val colorHex: String,
    val emoji: String,
    val memberCount: Int = 0
)

val TEAM_PRESET_PALETTE: List<Pair<String, String>> = listOf(
    "#EF4444" to "🔴", // Red
    "#3B82F6" to "🔵", // Blue
    "#10B981" to "🟢", // Emerald
    "#F59E0B" to "🟡", // Amber
    "#8B5CF6" to "🟣", // Purple
    "#EC4899" to "🌸", // Pink
    "#06B6D4" to "💎", // Cyan
    "#F97316" to "🔥", // Orange
    "#84CC16" to "🍏", // Lime
    "#14B8A6" to "🦚", // Teal
    "#6366F1" to "⚡", // Indigo
    "#D946EF" to "🦄", // Fuchsia
    "#E11D48" to "🌹", // Rose
    "#0284C7" to "🌊", // Sky
    "#059669" to "🍀", // Green
    "#D97706" to "🐯", // Gold
    "#7C3AED" to "👾", // Violet
    "#DB2777" to "🍧", // Pink-Red
    "#0891B2" to "🧊", // Dark Cyan
    "#4F46E5" to "🚀"  // Deep Indigo
)

@Serializable
data class Player(
    val id: String,
    val name: String,
    val avatar: String,
    val score: Int = 0,
    val streak: Int = 0,
    val isVip: Boolean = false,
    val isConnected: Boolean = true,
    val teamId: String? = null,
    val teamName: String? = null,
    val lastAnswer: PlayerAnswer? = null
)

@Serializable
data class PlayerAnswer(
    val optionIndex: Int,
    val timeElapsedMs: Long,
    val isCorrect: Boolean = false,
    val pointsEarned: Int = 0
)

@Serializable
data class Question(
    val id: String,
    val text: String,
    val textPl: String? = null,
    val imageAsset: String? = null,
    val timeLimitSeconds: Int = 20,
    val options: List<String>,
    val optionsPl: List<String>? = null,
    val correctIndex: Int,
    val funFact: String = "",
    val funFactPl: String? = null,
    val melodyTune: String? = null,
    val rebusEmoji: String? = null
) {
    fun localizedText(lang: AppLanguage): String =
        if (lang == AppLanguage.PL && !textPl.isNullOrBlank()) textPl else text

    fun localizedOptions(lang: AppLanguage): List<String> =
        if (lang == AppLanguage.PL && !optionsPl.isNullOrEmpty() && optionsPl.size == options.size) optionsPl else options

    fun localizedFunFact(lang: AppLanguage): String =
        if (lang == AppLanguage.PL && !funFactPl.isNullOrBlank()) funFactPl else funFact
}

@Serializable
data class QuestionPack(
    val id: String,
    val version: Int = 1,
    val title: String,
    val titlePl: String? = null,
    val description: String = "",
    val descriptionPl: String? = null,
    val ageGroup: GameMode = GameMode.ADULTS,
    val category: String = "General",
    val accentColor: String = "#8B5CF6",
    val questions: List<Question> = emptyList()
) {
    fun localizedTitle(lang: AppLanguage): String =
        if (lang == AppLanguage.PL && !titlePl.isNullOrBlank()) titlePl else title

    fun localizedDescription(lang: AppLanguage): String =
        if (lang == AppLanguage.PL && !descriptionPl.isNullOrBlank()) descriptionPl else description
}

@Serializable
data class PlayerStanding(
    val rank: Int,
    val playerId: String,
    val name: String,
    val avatar: String,
    val score: Int,
    val streak: Int,
    val lastScoreDiff: Int = 0,
    val teamId: String? = null,
    val teamName: String? = null
)

@Serializable
data class TeamStanding(
    val rank: Int,
    val teamId: String,
    val name: String,
    val colorHex: String,
    val emoji: String,
    val totalScore: Int,
    val playersCount: Int
)
