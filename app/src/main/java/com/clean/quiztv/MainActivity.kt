package com.clean.quiztv

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.clean.quiztv.domain.model.AppLanguage
import com.clean.quiztv.domain.model.LobbyStep
import com.clean.quiztv.game.GameState
import com.clean.quiztv.ui.screens.LeaderboardScreen
import com.clean.quiztv.ui.screens.LobbyScreen
import com.clean.quiztv.ui.screens.PodiumScreen
import com.clean.quiztv.ui.screens.QuestionScreen
import com.clean.quiztv.ui.screens.RevealScreen
import com.clean.quiztv.ui.theme.QuizTVTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var app: QuizApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        app = application as QuizApplication
        val stateMachine = app.stateMachine
        val server = app.server

        setContent {
            QuizTVTheme {
                val state by stateMachine.state.collectAsState()
                val scope = rememberCoroutineScope()

                // High-End Agency Ambient Background (Radial Mesh Lighting)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF0F224A),
                                    Color(0xFF081022),
                                    Color(0xFF04060C)
                                ),
                                center = Offset(960f, 180f),
                                radius = 1700f
                            )
                        )
                ) {
                    when (val s = state) {
                        is GameState.Lobby -> {
                            LobbyScreen(
                                state = s,
                                serverUrl = server.serverUrl,
                                onLanguageSelect = { lang ->
                                    scope.launch { stateMachine.setLanguage(lang) }
                                },
                                onModeSelect = { mode ->
                                    scope.launch { stateMachine.selectMode(mode) }
                                },
                                onFormatSelect = { format ->
                                    scope.launch { stateMachine.selectFormat(format) }
                                },
                                onStepChange = { step ->
                                    scope.launch { stateMachine.setLobbyStep(step) }
                                },
                                onPackSelect = { packId ->
                                    scope.launch { stateMachine.selectPack(packId) }
                                },
                                onStartGame = { packId, mode ->
                                    scope.launch { stateMachine.startGame(packId, mode) }
                                }
                            )
                        }
                        is GameState.QuestionRound -> {
                            QuestionScreen(state = s)
                        }
                        is GameState.Reveal -> {
                            RevealScreen(state = s)
                        }
                        is GameState.Leaderboard -> {
                            LeaderboardScreen(
                                state = s,
                                onProceed = {
                                    scope.launch { stateMachine.proceedAfterLeaderboard() }
                                }
                            )
                        }
                        is GameState.Podium -> {
                            PodiumScreen(
                                state = s,
                                onPlayAgain = {
                                    scope.launch { stateMachine.resetToLobby() }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Multi-screen hierarchical Back navigation on TV remote
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_BACK) {
            val state = app.stateMachine.state.value
            if (state is GameState.Lobby) {
                when (state.step) {
                    LobbyStep.THEME_SELECT -> {
                        lifecycleScope.launch { app.stateMachine.setLobbyStep(LobbyStep.CONNECT) }
                        return true
                    }
                    LobbyStep.CONNECT -> {
                        lifecycleScope.launch { app.stateMachine.setLobbyStep(LobbyStep.SETUP) }
                        return true
                    }
                    LobbyStep.SETUP -> {
                        return super.dispatchKeyEvent(event)
                    }
                }
            } else {
                lifecycleScope.launch {
                    app.stateMachine.resetToLobby()
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
