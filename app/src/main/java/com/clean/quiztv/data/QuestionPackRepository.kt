package com.clean.quiztv.data

import android.content.Context
import com.clean.quiztv.domain.model.AppLanguage
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.QuestionPack
import com.clean.quiztv.domain.model.Strings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

interface IQuestionPackRepository {
    suspend fun getAllPacks(): List<QuestionPack>
    suspend fun getPackById(id: String): QuestionPack?
    suspend fun getPreparedRoundPack(packId: String, count: Int = 10): QuestionPack?
    suspend fun savePack(pack: QuestionPack): Boolean
}

class QuestionPackRepository(
    private val context: Context,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }
) : IQuestionPackRepository {
    val historyManager = QuestionHistoryManager(context)
    private val customPacksDir = File(context.filesDir, "packs").apply { mkdirs() }
    private val cacheMutex = Mutex()
    private var cachedPacks: List<QuestionPack>? = null

    override suspend fun getAllPacks(): List<QuestionPack> = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            cachedPacks?.let { return@withContext it }

            val list = mutableListOf<QuestionPack>()

            // 1. Built-in packs in assets/packs/
            try {
                val assetFiles = (context.assets.list("packs") ?: emptyArray()).sorted()
                for (filename in assetFiles) {
                    if (filename.endsWith(".json")) {
                        try {
                            context.assets.open("packs/$filename").bufferedReader().use { reader ->
                                val pack = json.decodeFromString<QuestionPack>(reader.readText())
                                list.add(pack)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2. Custom packs in filesDir/packs/
            val customFiles = customPacksDir.listFiles { f -> f.extension == "json" } ?: emptyArray()
            for (file in customFiles) {
                try {
                    file.bufferedReader().use { reader ->
                        val pack = json.decodeFromString<QuestionPack>(reader.readText())
                        if (list.none { it.id == pack.id }) {
                            list.add(pack)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 3. Add Virtual "Random Mix" packs for both Adults and Kids
            val adultsQuestions = list.filter { it.ageGroup == GameMode.ADULTS }.flatMap { it.questions }
            val kidsQuestions = list.filter { it.ageGroup == GameMode.KIDS }.flatMap { it.questions }

            val virtualAdultsMix = QuestionPack(
                id = "random_mix_adults",
                version = 2,
                title = Strings.randomMixTitle(AppLanguage.RU),
                titlePl = Strings.randomMixTitle(AppLanguage.PL),
                description = Strings.randomMixDesc(AppLanguage.RU),
                descriptionPl = Strings.randomMixDesc(AppLanguage.PL),
                ageGroup = GameMode.ADULTS,
                category = "Mix",
                accentColor = "#F43F5E",
                questions = adultsQuestions
            )

            val virtualKidsMix = QuestionPack(
                id = "random_mix_kids",
                version = 2,
                title = "Детский Турнирный Микс",
                titlePl = "Dziecięcy Mix Turniejowy",
                description = "2000 проверенных вопросов из всех детских и семейных рубрик без повторений.",
                descriptionPl = "2000 sprawdzonych pytań ze wszystkich kategorii dla dzieci bez powtórzeń.",
                ageGroup = GameMode.KIDS,
                category = "Mix",
                accentColor = "#10B981",
                questions = kidsQuestions
            )

            val result = list + listOf(virtualAdultsMix, virtualKidsMix)
            cachedPacks = result
            result
        }
    }

    override suspend fun getPackById(id: String): QuestionPack? = withContext(Dispatchers.IO) {
        getAllPacks().find { it.id == id }
    }

    /**
     * Retrieves a pack with fresh questions, filtered against question history
     * to eliminate duplicate questions between games.
     */
    override suspend fun getPreparedRoundPack(packId: String, count: Int): QuestionPack? = withContext(Dispatchers.IO) {
        val basePack = getPackById(packId) ?: return@withContext null
        val freshQuestions = historyManager.selectFreshQuestions(basePack.questions, count = count)

        // Mark chosen questions as seen so next game picks different ones
        historyManager.markQuestionsSeen(freshQuestions.map { it.id })

        basePack.copy(questions = freshQuestions)
    }

    override suspend fun savePack(pack: QuestionPack): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(customPacksDir, "${pack.id}.json")
            val content = json.encodeToString(QuestionPack.serializer(), pack)
            file.writeText(content)
            cacheMutex.withLock {
                cachedPacks = null
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
