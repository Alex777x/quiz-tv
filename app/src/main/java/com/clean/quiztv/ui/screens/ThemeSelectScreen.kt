package com.clean.quiztv.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clean.quiztv.audio.TvSoundManager
import com.clean.quiztv.domain.model.AppLanguage
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.QuestionPack
import com.clean.quiztv.domain.model.Strings
import com.clean.quiztv.game.GameState
import com.clean.quiztv.ui.components.DoubleBezelCard
import com.clean.quiztv.ui.theme.BorderFocused
import com.clean.quiztv.ui.theme.BorderLight
import com.clean.quiztv.ui.theme.NeonAmber
import com.clean.quiztv.ui.theme.NeonCyan
import com.clean.quiztv.ui.theme.NeonEmerald
import com.clean.quiztv.ui.theme.NeonViolet
import com.clean.quiztv.ui.theme.TextMuted

@Composable
fun ThemeSelectScreen(
    state: GameState.Lobby,
    onPackSelect: (String) -> Unit,
    onBackToLobby: () -> Unit,
    onStartGame: (packId: String, mode: GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val packsForMode = state.availablePacks.filter { it.ageGroup == state.selectedMode }
    val currentSelectedId = state.selectedPackId
        ?: packsForMode.firstOrNull()?.id
        ?: state.availablePacks.firstOrNull()?.id

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Strings.themeSelectTitle(lang),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Strings.themeSelectSubtitle(lang),
                    color = TextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mode Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (state.selectedMode == GameMode.KIDS) NeonAmber.copy(alpha = 0.2f)
                            else NeonCyan.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = Strings.modeBadge(state.selectedMode, lang),
                        color = if (state.selectedMode == GameMode.KIDS) NeonAmber else NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Players Count Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x3338BDF8))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "👥 ${state.players.size}",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Large Fullscreen Theme Cards Carousel ---
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(packsForMode, key = { it.id }) { pack ->
                val isSelected = (pack.id == currentSelectedId)
                ThemeCard(
                    pack = pack,
                    lang = lang,
                    isSelected = isSelected,
                    onSelect = { onPackSelect(pack.id) }
                )
            }
        }

        // --- Bottom Actions: Back to Lobby & Start Quiz ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            var isBackFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .scale(if (isBackFocused) 1.05f else 1.0f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x331E293B))
                    .border(
                        width = if (isBackFocused) 2.5.dp else 1.dp,
                        color = if (isBackFocused) BorderFocused else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !isBackFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        isBackFocused = focusState.isFocused
                    }
                    .focusable()
                    .clickable {
                        TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        onBackToLobby()
                    }
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                Text(
                    text = Strings.btnBackToLobby(lang),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Start Quiz Primary Button
            val canStart = currentSelectedId != null && state.players.isNotEmpty()
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            var isStartFocused by remember { mutableStateOf(false) }
            val startScale by animateFloatAsState(
                targetValue = if (isStartFocused) 1.06f else 1.0f,
                animationSpec = tween(durationMillis = 200),
                label = "btnStartQuizScale"
            )

            Box(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .scale(startScale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (canStart) Brush.horizontalGradient(listOf(NeonEmerald, Color(0xFF047857)))
                        else Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                    )
                    .border(
                        width = if (isStartFocused) 3.dp else 1.dp,
                        color = if (isStartFocused) BorderFocused else BorderLight,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !isStartFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        isStartFocused = focusState.isFocused
                    }
                    .focusable()
                    .clickable(enabled = canStart) {
                        if (currentSelectedId != null) {
                            TvSoundManager.play(TvSoundManager.SoundEffect.CORRECT)
                            onStartGame(currentSelectedId, state.selectedMode)
                        }
                    }
                    .padding(horizontal = 36.dp, vertical = 15.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Strings.btnStartQuizNow(lang),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "[OK]",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    pack: QuestionPack,
    lang: AppLanguage,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) 1.03f else if (isSelected) 1.01f else 1.0f,
        animationSpec = tween(durationMillis = 180),
        label = "themeCardScale"
    )

    // Thematic emoji resolver
    val emoji = remember(pack.id) {
        when {
            pack.id.contains("cinema") -> "🎬"
            pack.id.contains("melody") || pack.id.contains("music") -> "🎵"
            pack.id.contains("rebus") -> "🧩"
            pack.id.contains("geography") || pack.id.contains("poland") -> "🌍"
            pack.id.contains("90s") || pack.id.contains("popculture") -> "🕹️"
            pack.id.contains("auto") || pack.id.contains("cars") -> "🏎️"
            pack.id.contains("game") -> "🎮"
            pack.id.contains("animals") -> "🦁"
            pack.id.contains("cartoons") -> "🎈"
            pack.id.contains("logic") -> "💡"
            pack.id.contains("random") || pack.id.contains("mix") -> "🎲"
            else -> "✨"
        }
    }

    Box(
        modifier = Modifier
            .width(220.dp)
            .height(260.dp)
            .scale(animatedScale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                else Brush.verticalGradient(listOf(Color(0x330F172A), Color(0x330B0F19)))
            )
            .border(
                width = if (isFocused) 2.5.dp else if (isSelected) 2.dp else 1.dp,
                color = if (isFocused) BorderFocused else if (isSelected) NeonCyan else Color(0x22FFFFFF),
                shape = RoundedCornerShape(20.dp)
            )
            .onFocusChanged { focusState ->
                if (focusState.isFocused && !isFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                isFocused = focusState.isFocused
            }
            .focusable()
            .clickable {
                TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                onSelect()
            }
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Emoji & Selection Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.radialGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonCyan.copy(alpha = 0.2f))
                            .border(1.dp, NeonCyan, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = Strings.selectedThemeBadge(lang),
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Title & Description
            Column {
                Text(
                    text = pack.localizedTitle(lang),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pack.localizedDescription(lang),
                    color = TextMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom Badges: Question Count & Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33475569))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${pack.questions.size} ${if (lang == AppLanguage.PL) "pytań" else "вопр."}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "⏱️ 20s",
                    color = TextMuted,
                    fontSize = 10.5.sp
                )
            }
        }
    }
}
