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
    "#EF4444" to "01",
    "#3B82F6" to "02",
    "#10B981" to "03",
    "#F59E0B" to "04",
    "#8B5CF6" to "05",
    "#EC4899" to "06",
    "#06B6D4" to "07",
    "#F97316" to "08",
    "#84CC16" to "09",
    "#14B8A6" to "10",
    "#6366F1" to "11",
    "#D946EF" to "12",
    "#E11D48" to "13",
    "#0284C7" to "14",
    "#059669" to "15",
    "#D97706" to "16",
    "#7C3AED" to "17",
    "#DB2777" to "18",
    "#0891B2" to "19",
    "#4F46E5" to "20"
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
