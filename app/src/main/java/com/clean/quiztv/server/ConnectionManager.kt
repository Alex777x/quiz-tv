package com.clean.quiztv.server

import com.clean.quiztv.domain.protocol.ClientMessage
import com.clean.quiztv.domain.protocol.ServerMessage
import com.clean.quiztv.game.GameStateMachine
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class ConnectionManager(
    private val stateMachine: GameStateMachine,
    private val coroutineScope: CoroutineScope,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
) {
    private val mutex = Mutex()
    private val activeSessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
    private val sessionToPlayerId = ConcurrentHashMap<DefaultWebSocketServerSession, String>()

    init {
        // Listen to outgoing state machine events and forward to WebSocket clients
        stateMachine.outgoingMessages
            .onEach { (recipientId, message) ->
                val jsonText = json.encodeToString(ServerMessage.serializer(), message)
                if (recipientId != null) {
                    sendToPlayer(recipientId, jsonText)
                } else {
                    broadcast(jsonText)
                }
            }
            .launchIn(coroutineScope)
    }

    suspend fun handleSession(session: DefaultWebSocketServerSession) {
        try {
            for (frame in session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    handleIncomingFrame(session, text)
                }
            }
        } catch (_: ClosedReceiveChannelException) {
            // Normal client disconnect
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            onSessionClosed(session)
        }
    }

    private suspend fun handleIncomingFrame(session: DefaultWebSocketServerSession, text: String) {
        val message = try {
            json.decodeFromString(ClientMessage.serializer(), text)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        when (message) {
            is ClientMessage.Join -> {
                val player = stateMachine.handleJoin(message.playerId, message.name, message.avatar)
                registerPlayerSession(player.id, session)
            }
            is ClientMessage.Reconnect -> {
                val success = stateMachine.handleReconnect(message.playerId)
                if (success) {
                    registerPlayerSession(message.playerId, session)
                } else {
                    // Session unknown or expired
                    val err = ServerMessage.Error("Сессия не найдена. Пожалуйста, войдите снова.")
                    val raw = json.encodeToString(ServerMessage.serializer(), err)
                    session.send(Frame.Text(raw))
                }
            }
            is ClientMessage.SetLanguage -> {
                if (!isVipSession(session)) return
                stateMachine.setLanguage(message.language)
            }
            is ClientMessage.SelectMode -> {
                if (!isVipSession(session)) return
                stateMachine.selectMode(message.mode)
            }
            is ClientMessage.SelectPack -> {
                if (!isVipSession(session)) return
                stateMachine.selectPack(message.packId)
            }
            is ClientMessage.SelectFormat -> {
                if (!isVipSession(session)) return
                stateMachine.selectFormat(message.format)
            }
            is ClientMessage.CreateTeam -> {
                val playerId = sessionToPlayerId[session]
                if (playerId != null) {
                    stateMachine.createTeam(playerId, message.name)
                }
            }
            is ClientMessage.SelectTeam -> {
                val playerId = sessionToPlayerId[session]
                if (playerId != null) {
                    stateMachine.selectTeam(playerId, message.teamId)
                }
            }
            is ClientMessage.GoToStep -> {
                if (!isVipSession(session)) return
                try {
                    val step = com.clean.quiztv.domain.model.LobbyStep.valueOf(message.step)
                    stateMachine.setLobbyStep(step)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            is ClientMessage.LeaveRoom -> {
                val playerId = sessionToPlayerId.remove(session)
                if (playerId != null) {
                    mutex.withLock {
                        activeSessions.remove(playerId)
                    }
                    stateMachine.handleLeave(playerId)
                }
            }
            is ClientMessage.StartGame -> {
                if (!isVipSession(session)) return
                stateMachine.startGame(message.packId, message.gameMode)
            }
            is ClientMessage.SubmitAnswer -> {
                val playerId = sessionToPlayerId[session]
                if (playerId != null) {
                    stateMachine.submitAnswer(
                        playerId = playerId,
                        questionId = message.questionId,
                        optionIndex = message.optionIndex,
                        timeElapsedMs = message.timeElapsedMs
                    )
                }
            }
            is ClientMessage.TogglePause -> {
                if (!isVipSession(session)) return
                stateMachine.togglePause()
            }
            is ClientMessage.ForceReveal -> {
                if (!isVipSession(session)) return
                stateMachine.forceReveal()
            }
            is ClientMessage.ReturnToLobby -> {
                if (!isVipSession(session)) return
                stateMachine.resetToLobby()
            }
            is ClientMessage.NextQuestion -> {
                if (!isVipSession(session)) return
                stateMachine.advanceGame()
            }
            is ClientMessage.PlayAgain -> {
                if (!isVipSession(session)) return
                stateMachine.resetToLobby()
            }
        }
    }

    private fun isVipSession(session: DefaultWebSocketServerSession): Boolean {
        val playerId = sessionToPlayerId[session] ?: return false
        return stateMachine.isPlayerVip(playerId)
    }

    private suspend fun registerPlayerSession(playerId: String, session: DefaultWebSocketServerSession) = mutex.withLock {
        // If there was an old session for this player, close it gracefully
        activeSessions[playerId]?.let { oldSession ->
            if (oldSession != session) {
                coroutineScope.launch {
                    try {
                        oldSession.close()
                    } catch (_: Exception) {}
                }
            }
        }
        activeSessions[playerId] = session
        sessionToPlayerId[session] = playerId
    }

    private suspend fun onSessionClosed(session: DefaultWebSocketServerSession) {
        val playerId = sessionToPlayerId.remove(session)
        if (playerId != null) {
            mutex.withLock {
                if (activeSessions[playerId] == session) {
                    activeSessions.remove(playerId)
                }
            }
            stateMachine.handleDisconnect(playerId)
        }
    }

    private suspend fun sendToPlayer(playerId: String, jsonText: String) {
        val session = activeSessions[playerId] ?: return
        try {
            session.send(Frame.Text(jsonText))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun broadcast(jsonText: String) {
        val frame = Frame.Text(jsonText)
        val sessions: Collection<DefaultWebSocketServerSession> = activeSessions.values
        for (session in sessions) {
            try {
                session.send(frame)
            } catch (_: Exception) {
                // Individual failed session handled by handleSession finally
            }
        }
    }
}
