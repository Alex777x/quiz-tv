package com.clean.quiztv.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.clean.quiztv.domain.model.AppLanguage
import com.clean.quiztv.domain.model.GameFormat
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.LobbyStep
import com.clean.quiztv.game.GameState

/**
 * Multi-Screen Pre-Game Coordinator.
 * Cleanly separates the setup flow across 3 focused, spacious screens:
 * 1. [SetupScreen]: Language, Kids/Adults, Solo/Teams selection
 * 2. [RoomLobbyScreen]: Room QR code, server address & joining players / team columns
 * 3. [ThemeSelectScreen]: Fullscreen thematic showcase & quiz start
 */
@Composable
fun LobbyScreen(
    state: GameState.Lobby,
    serverUrl: String,
    onLanguageSelect: (AppLanguage) -> Unit,
    onModeSelect: (GameMode) -> Unit,
    onFormatSelect: (GameFormat) -> Unit,
    onStepChange: (LobbyStep) -> Unit,
    onPackSelect: (String) -> Unit,
    onStartGame: (packId: String, mode: GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = state.step,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
        },
        label = "lobbyStepTransition"
    ) { currentStep ->
        when (currentStep) {
            LobbyStep.SETUP -> {
                SetupScreen(
                    currentLanguage = state.language,
                    selectedMode = state.selectedMode,
                    selectedFormat = state.gameFormat,
                    onLanguageSelect = onLanguageSelect,
                    onModeSelect = onModeSelect,
                    onFormatSelect = onFormatSelect,
                    onProceedToLobby = { onStepChange(LobbyStep.CONNECT) },
                    modifier = modifier
                )
            }
            LobbyStep.CONNECT -> {
                RoomLobbyScreen(
                    state = state,
                    serverUrl = serverUrl,
                    onBackToSetup = { onStepChange(LobbyStep.SETUP) },
                    onProceedToThemeSelect = { onStepChange(LobbyStep.THEME_SELECT) },
                    modifier = modifier
                )
            }
            LobbyStep.THEME_SELECT -> {
                ThemeSelectScreen(
                    state = state,
                    onPackSelect = onPackSelect,
                    onBackToLobby = { onStepChange(LobbyStep.CONNECT) },
                    onStartGame = onStartGame,
                    modifier = modifier
                )
            }
        }
    }
}
