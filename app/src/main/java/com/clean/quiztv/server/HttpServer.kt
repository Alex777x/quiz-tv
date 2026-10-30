package com.clean.quiztv.server

import android.content.Context
import com.clean.quiztv.data.llm.LlmService
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.game.GameStateMachine
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.InputStream
import java.time.Duration

class HttpServer(
    private val context: Context,
    private val stateMachine: GameStateMachine,
    private val llmService: LlmService,
    val port: Int = 8080
) {
    private val serverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionManager = ConnectionManager(stateMachine, serverScope)
    private var engine: ApplicationEngine? = null
    private val assetCache = java.util.concurrent.ConcurrentHashMap<String, ByteArray>()

    val localIp: String
        get() = NetworkUtils.getLocalIpAddress(context)

    val serverUrl: String
        get() = "http://$localIp:$port"

    fun start() {
        if (engine != null) return
        serverScope.launch {
            startEngineInternal()
        }
    }

    private fun startEngineInternal() {
        if (engine != null) return

        engine = embeddedServer(CIO, port = port, host = "0.0.0.0") {
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(30)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {
                // WebSocket endpoint for real-time mobile gameplay
                webSocket("/ws/game") {
                    connectionManager.handleSession(this)
                }

                // Health check & status
                get("/api/status") {
                    call.respondText(
                        """{"status":"ok","ip":"$localIp","port":$port}""",
                        ContentType.Application.Json
                    )
                }

                // LLM Pack Generation API (called from VIP phone or TV UI)
                post("/api/generate-pack") {
                    try {
                        val bodyText = call.receiveText()
                        val jsonObj = Json.parseToJsonElement(bodyText).jsonObject
                        val topic = jsonObj["topic"]?.jsonPrimitive?.content ?: ""
                        val apiKey = jsonObj["apiKey"]?.jsonPrimitive?.content ?: ""
                        val baseUrl = jsonObj["baseUrl"]?.jsonPrimitive?.content ?: "https://api.openai.com/v1"
                        val model = jsonObj["model"]?.jsonPrimitive?.content ?: "gpt-4o-mini"
                        val ageGroupStr = jsonObj["ageGroup"]?.jsonPrimitive?.content ?: "ADULTS"
                        val isGemini = jsonObj["isGemini"]?.jsonPrimitive?.booleanOrNull ?: false

                        if (topic.isBlank() || apiKey.isBlank()) {
                            call.respond(HttpStatusCode.BadRequest, """{"error":"Missing topic or apiKey"}""")
                            return@post
                        }

                        val mode = if (ageGroupStr.equals("KIDS", ignoreCase = true)) GameMode.KIDS else GameMode.ADULTS
                        val result = llmService.generatePack(
                            topic = topic,
                            ageGroup = mode,
                            apiKey = apiKey,
                            baseUrl = baseUrl,
                            model = model,
                            isGemini = isGemini
                        )

                        result.onSuccess { pack ->
                            stateMachine.refreshPacks()
                            call.respondText(
                                """{"success":true,"status":"ok","packId":"${pack.id}","title":"${pack.title}"}""",
                                ContentType.Application.Json
                            )
                        }.onFailure { err ->
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                """{"error":"${err.message?.replace("\"", "\\\"")}"}"""
                            )
                        }
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            """{"error":"${e.message?.replace("\"", "\\\"")}"}"""
                        )
                    }
                }

                // Serve mobile web controller from Android assets/web/
                get("/") {
                    serveAsset(call, "web/index.html", ContentType.Text.Html)
                }
                get("/index.html") {
                    serveAsset(call, "web/index.html", ContentType.Text.Html)
                }
                get("/style.css") {
                    serveAsset(call, "web/style.css", ContentType.Text.CSS)
                }
                get("/app.js") {
                    serveAsset(call, "web/app.js", ContentType.Application.JavaScript)
                }
                get("/icon.png") {
                    serveAsset(call, "web/icon.png", ContentType.Image.PNG)
                }
                get("/favicon.ico") {
                    serveAsset(call, "web/icon.png", ContentType.Image.PNG)
                }

                // Serve any other web assets or icons
                get("/assets/{path...}") {
                    val subPath = call.parameters.getAll("path")?.joinToString("/") ?: ""
                    if (subPath.contains("..") || subPath.startsWith("/")) {
                        call.respond(HttpStatusCode.Forbidden, "Invalid asset path")
                        return@get
                    }
                    val contentType = when {
                        subPath.endsWith(".html") -> ContentType.Text.Html
                        subPath.endsWith(".css") -> ContentType.Text.CSS
                        subPath.endsWith(".js") -> ContentType.Application.JavaScript
                        subPath.endsWith(".png") -> ContentType.Image.PNG
                        subPath.endsWith(".webp") -> ContentType.parse("image/webp")
                        subPath.endsWith(".svg") -> ContentType.Image.SVG
                        subPath.endsWith(".json") -> ContentType.Application.Json
                        else -> ContentType.Application.OctetStream
                    }
                    serveAsset(call, "web/$subPath", contentType)
                }
            }
        }.start(wait = false)
    }

    private suspend fun serveAsset(
        call: ApplicationCall,
        assetPath: String,
        contentType: ContentType
    ) {
        val cached = assetCache[assetPath]
        if (cached != null) {
            call.respondBytes(cached, contentType)
            return
        }
        var stream: InputStream? = null
        try {
            stream = context.assets.open(assetPath)
            val bytes = stream.readBytes()
            assetCache[assetPath] = bytes
            call.respondBytes(bytes, contentType)
        } catch (_: Exception) {
            call.respond(HttpStatusCode.NotFound, "File not found: $assetPath")
        } finally {
            try {
                stream?.close()
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        try {
            engine?.stop(1000, 2000)
            engine = null
            serverScope.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
