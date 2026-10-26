package com.clean.quiztv

import com.clean.quiztv.domain.model.QuestionPack
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuestionPackValidationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val assetsDir = File("src/main/assets")
    private val packsDir = File("src/main/assets/packs")

    @Test
    fun `validate all question packs in assets`() {
        val packFiles = packsDir.listFiles { _, name -> name.endsWith(".json") }
        assertNotNull("Packs directory should exist and contain JSON files", packFiles)
        assertTrue("At least 5 question packs should be present", (packFiles?.size ?: 0) >= 5)

        for (packFile in packFiles!!) {
            val content = packFile.readText(Charsets.UTF_8)
            val pack = json.decodeFromString<QuestionPack>(content)

            assertTrue("Pack ${pack.id} must have a non-empty title", pack.title.isNotBlank())
            assertTrue("Pack ${pack.id} must have at least 10 questions", pack.questions.size >= 10)

            val correctIndexHistogram = mutableMapOf<Int, Int>()

            pack.questions.forEachIndexed { qIdx, q ->
                val qRef = "[Pack: ${pack.id}, Question: ${q.id} (index $qIdx)]"

                // 1. Exactly 4 options in Russian
                assertEquals("$qRef options count must be 4", 4, q.options.size)

                // 2. Exactly 4 options in Polish (if present)
                if (q.optionsPl != null) {
                    assertEquals("$qRef optionsPl count must be 4", 4, q.optionsPl!!.size)
                }

                // 3. correctIndex in 0..3
                assertTrue("$qRef correctIndex must be between 0 and 3", q.correctIndex in 0..3)

                // 4. Anti-spoiler enforcement:
                // Correct answer cannot be the ONLY option with parentheses
                val parenInRu = q.options.indices.filter { q.options[it].contains("(") }
                assertNotEquals(
                    "$qRef SPOILER: Correct answer is the only option with parentheses in RU: ${q.options[q.correctIndex]}",
                    listOf(q.correctIndex),
                    parenInRu
                )

                if (q.optionsPl != null) {
                    val parenInPl = q.optionsPl!!.indices.filter { q.optionsPl!![it].contains("(") }
                    assertNotEquals(
                        "$qRef SPOILER: Correct answer is the only option with parentheses in PL: ${q.optionsPl!![q.correctIndex]}",
                        listOf(q.correctIndex),
                        parenInPl
                    )
                }

                // 5. Physical existence of referenced imageAsset
                if (q.imageAsset != null) {
                    val imgFile = File(assetsDir, q.imageAsset!!)
                    assertTrue(
                        "$qRef Referenced imageAsset file does not exist: ${imgFile.path}",
                        imgFile.exists() && imgFile.isFile
                    )
                }

                // 6. Physical existence of referenced melodyTune
                if (q.melodyTune != null) {
                    val tune = q.melodyTune!!
                    val candidatePaths = listOf(
                        File(assetsDir, tune),
                        File(assetsDir, "audio/$tune"),
                        File(assetsDir, "audio/$tune.m4a"),
                        File(assetsDir, "audio/$tune.mp3")
                    )
                    val exists = candidatePaths.any { it.exists() && it.isFile }
                    assertTrue(
                        "$qRef Referenced melodyTune audio file does not exist in assets: $tune",
                        exists
                    )
                }

                // Track distribution
                correctIndexHistogram[q.correctIndex] = (correctIndexHistogram[q.correctIndex] ?: 0) + 1
            }

            // 7. Verify balanced distribution of correct answers across options 0..3
            for (optionIdx in 0..3) {
                val count = correctIndexHistogram[optionIdx] ?: 0
                assertTrue(
                    "Pack ${pack.id} must have at least 1 question where option $optionIdx is correct (was $count)",
                    count >= 1
                )
            }
        }
    }
}
