package com.clean.quiztv.data.llm

import com.clean.quiztv.data.QuestionPackRepository
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.QuestionPack
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID

class LlmService(
    private val repository: QuestionPackRepository,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
) {
    private val client = HttpClient(CIO) {
        engine {
            requestTimeout = 60_000L
        }
    }

    /**
     * Generates a new quiz pack via OpenAI-compatible endpoint or Google Gemini API.
     */
    suspend fun generatePack(
        topic: String,
        ageGroup: GameMode = GameMode.ADULTS,
        questionCount: Int = 8,
        apiKey: String,
        baseUrl: String = "https://api.openai.com/v1",
        model: String = "gpt-4o-mini",
        isGemini: Boolean = false
    ): Result<QuestionPack> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """
                Ты профессиональный автор веселых викторин и квизов в стиле Kahoot и Jackbox.
                Создай пак из $questionCount интересных, остроумных вопросов на тему: "$topic".
                Возрастная категория: ${if (ageGroup == GameMode.KIDS) "ДЕТИ (7-12 лет, добрые, понятные вопросы)" else "ВЗРОСЛЫЕ (18+, азартные, с изюминкой)"}.

                Требования:
                - Ровно 4 варианта ответа на каждый вопрос.
                - correctIndex — число от 0 до 3 (индекс правильного ответа).
                - funFact — короткий поразительный или забавный факт, объясняющий правильный ответ (1-2 предложения).
                - timeLimitSeconds: ${if (ageGroup == GameMode.KIDS) 25 else 20}.
                - Двуязычность: сгенерируй русские поля (title, description, text, options, funFact) и качественный перевод на польский язык (titlePl, descriptionPl, textPl, optionsPl, funFactPl).

                Верни ТОЛЬКО валидный JSON без обертки ```json или пояснений по схеме:
                {
                  "id": "generated_${UUID.randomUUID().toString().take(8)}",
                  "version": 1,
                  "title": "Название темы на русском",
                  "titlePl": "Tytuł tematu po polsku",
                  "description": "Краткое яркое описание темы",
                  "descriptionPl": "Krótki, żywy opis tematu po polsku",
                  "ageGroup": "${ageGroup.name}",
                  "category": "$topic",
                  "accentColor": "#8B5CF6",
                  "questions": [
                    {
                      "id": "q1",
                      "text": "Текст вопроса на русском?",
                      "textPl": "Treść pytania po polsku?",
                      "timeLimitSeconds": ${if (ageGroup == GameMode.KIDS) 25 else 20},
                      "options": ["Вариант 1", "Вариант 2", "Вариант 3", "Вариант 4"],
                      "optionsPl": ["Opcja 1", "Opcja 2", "Opcja 3", "Opcja 4"],
                      "correctIndex": 0,
                      "funFact": "Текст факта на русском",
                      "funFactPl": "Ciekawostka po polsku"
                    }
                  ]
                }
            """.trimIndent()

            val rawJsonContent = if (isGemini) {
                callGemini(apiKey, model, systemPrompt)
            } else {
                callOpenAiCompatible(apiKey, baseUrl, model, systemPrompt)
            }

            val cleaned = cleanJsonString(rawJsonContent)
            val parsedPack = json.decodeFromString<QuestionPack>(cleaned)

            // Force unique question IDs scoped to pack id so QuestionHistoryManager does not mark them as already seen
            val updatedQuestions = parsedPack.questions.mapIndexed { index, q ->
                q.copy(id = "${parsedPack.id}_q_${index + 1}")
            }
            val pack = parsedPack.copy(questions = updatedQuestions)

            // Save to filesDir so it's instantly available in the lobby
            repository.savePack(pack)

            Result.success(pack)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun callOpenAiCompatible(
        apiKey: String,
        baseUrl: String,
        model: String,
        prompt: String
    ): String {
        val endpoint = "${baseUrl.trimEnd('/')}/chat/completions"
        val payload = buildJsonObject {
            put("model", model)
            put("messages", buildJsonArray {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("temperature", 0.7)
        }

        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $apiKey")
            setBody(payload.toString())
        }

        val responseText = response.bodyAsText()
        val parsed = json.parseToJsonElement(responseText).jsonObject
        val choices = parsed["choices"]?.jsonArray ?: throw IllegalStateException("Empty choices from LLM: $responseText")
        val content = choices[0].jsonObject["message"]?.jsonObject?.get("content")?.jsonPrimitive?.content
            ?: throw IllegalStateException("Empty content in LLM message")
        return content
    }

    private suspend fun callGemini(
        apiKey: String,
        model: String,
        prompt: String
    ): String {
        val modelName = if (model.startsWith("gemini")) model else "gemini-2.5-flash"
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val payload = buildJsonObject {
            put("contents", buildJsonArray {
                add(buildJsonObject {
                    put("parts", buildJsonArray {
                        add(buildJsonObject {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", buildJsonObject {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(payload.toString())
        }

        val responseText = response.bodyAsText()
        val parsed = json.parseToJsonElement(responseText).jsonObject
        val candidates = parsed["candidates"]?.jsonArray ?: throw IllegalStateException("No candidates in Gemini response: $responseText")
        val text = candidates[0].jsonObject["content"]?.jsonObject
            ?.get("parts")?.jsonArray?.get(0)?.jsonObject
            ?.get("text")?.jsonPrimitive?.content
            ?: throw IllegalStateException("No text in Gemini candidate")
        return text
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json").trim()
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```").trim()
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```").trim()
        }
        val firstBrace = str.indexOf('{')
        val lastBrace = str.lastIndexOf('}')
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            str = str.substring(firstBrace, lastBrace + 1)
        }
        return str.trim()
    }

    fun close() {
        client.close()
    }
}
