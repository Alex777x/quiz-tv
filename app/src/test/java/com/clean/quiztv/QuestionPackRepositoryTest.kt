package com.clean.quiztv

import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.QuestionPack
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuestionPackRepositoryTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun getAssetsPacksDir(): File {
        val candidates = listOf(
            File("src/main/assets/packs"),
            File("app/src/main/assets/packs"),
            File(System.getProperty("user.dir"), "src/main/assets/packs"),
            File(System.getProperty("user.dir"), "app/src/main/assets/packs")
        )
        return candidates.firstOrNull { it.exists() && it.isDirectory }
            ?: throw IllegalStateException("Cannot locate assets/packs directory in any of: ${candidates.map { it.absolutePath }}")
    }

    @Test
    fun testAllEightQuestionPacksAreValidAndBilingual() {
        val packsDir = getAssetsPacksDir()
        val jsonFiles = packsDir.listFiles { _, name -> name.endsWith(".json") }?.sortedBy { it.name }

        assertNotNull("JSON files list should not be null", jsonFiles)
        assertEquals("There should be exactly 8 JSON question packs in assets/packs", 8, jsonFiles!!.size)

        val allPackIds = mutableSetOf<String>()
        val allQuestionIds = mutableSetOf<String>()

        for (file in jsonFiles) {
            val content = file.readText(Charsets.UTF_8)
            val pack = try {
                json.decodeFromString<QuestionPack>(content)
            } catch (e: Exception) {
                throw AssertionError("Failed to parse JSON pack in file ${file.name}: ${e.message}", e)
            }

            // Pack Header Checks
            assertFalse("Pack id should not be blank in ${file.name}", pack.id.isBlank())
            assertTrue("Pack id should be unique, found duplicate: ${pack.id}", allPackIds.add(pack.id))

            assertFalse("Russian title should not be blank in ${file.name}", pack.title.isBlank())
            assertFalse("Polish title (titlePl) should not be blank in ${file.name}", pack.titlePl.isNullOrBlank())

            assertFalse("Russian description should not be blank in ${file.name}", pack.description.isBlank())
            assertFalse("Polish description (descriptionPl) should not be blank in ${file.name}", pack.descriptionPl.isNullOrBlank())

            assertTrue(
                "ageGroup must be KIDS or ADULTS in ${file.name}",
                pack.ageGroup == GameMode.KIDS || pack.ageGroup == GameMode.ADULTS
            )
            assertFalse("accentColor should not be blank in ${file.name}", pack.accentColor.isBlank())
            assertTrue("Pack ${pack.id} must contain questions", pack.questions.isNotEmpty())

            // Question Level Checks
            pack.questions.forEachIndexed { index, q ->
                val qRef = "Pack: ${pack.id}, Question #$index (id: ${q.id})"

                assertFalse("Question id must not be blank ($qRef)", q.id.isBlank())
                assertTrue("Question id must be globally unique ($qRef)", allQuestionIds.add(q.id))

                assertFalse("Question Russian text must not be blank ($qRef)", q.text.isBlank())
                assertFalse("Question Polish text (textPl) must not be blank ($qRef)", q.textPl.isNullOrBlank())

                // Exactly 4 options in Russian
                assertEquals("Question must have exactly 4 Russian options ($qRef)", 4, q.options.size)
                q.options.forEachIndexed { optIdx, opt ->
                    assertFalse("Option $optIdx must not be blank ($qRef)", opt.isBlank())
                }

                // Exactly 4 options in Polish
                assertNotNull("optionsPl must not be null ($qRef)", q.optionsPl)
                assertEquals("Question must have exactly 4 Polish options ($qRef)", 4, q.optionsPl!!.size)
                q.optionsPl!!.forEachIndexed { optIdx, optPl ->
                    assertFalse("OptionPl $optIdx must not be blank ($qRef)", optPl.isBlank())
                }

                // correctIndex in 0..3
                assertTrue(
                    "correctIndex must be in range 0..3, got ${q.correctIndex} ($qRef)",
                    q.correctIndex in 0..3
                )

                // funFact & funFactPl must be real explanations, never fake "Правильный ответ — ..."
                assertFalse("funFact must not be blank ($qRef)", q.funFact.isBlank())
                assertFalse("funFactPl must not be blank ($qRef)", q.funFactPl.isNullOrBlank())
                assertFalse(
                    "funFact must be a real fact, not 'Правильный ответ' ($qRef): ${q.funFact}",
                    q.funFact.lowercase().contains("правильный ответ")
                )

                // Strict ban on Russia/USSR content
                val fullText = "${q.text} ${q.options.joinToString(" ")} ${q.funFact}".lowercase()
                val bannedRegex = Regex("\\b(росси[яиюей]|российск\\w*|ссср|советск\\w*|москв\\w*|кремл\\w*|рубл\\w*|копейк\\w*|нтв|мосфильм)\\b")
                assertFalse(
                    "Question contains banned Russia/USSR reference ($qRef): ${q.text}",
                    bannedRegex.containsMatchIn(fullText)
                )

                // timeLimitSeconds
                assertTrue("timeLimitSeconds must be positive ($qRef)", q.timeLimitSeconds > 0)
            }
        }
    }
}
