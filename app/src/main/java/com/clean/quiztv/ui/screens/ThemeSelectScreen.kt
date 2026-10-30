package com.clean.quiztv.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clean.quiztv.audio.TvSoundManager
import com.clean.quiztv.domain.model.AppLanguage
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.QuestionPack
import com.clean.quiztv.domain.model.Strings
import com.clean.quiztv.game.GameState
import com.clean.quiztv.ui.theme.BorderFocused
import com.clean.quiztv.ui.theme.BorderLight
import com.clean.quiztv.ui.theme.NeonAmber
import com.clean.quiztv.ui.theme.NeonCyan
import com.clean.quiztv.ui.theme.NeonEmerald
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
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Strings.themeSelectSubtitle(lang),
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val modeColor = if (state.selectedMode == GameMode.KIDS) NeonAmber else NeonCyan
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(modeColor.copy(alpha = 0.16f))
                        .border(1.dp, modeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = Strings.modeBadge(state.selectedMode, lang),
                        color = modeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x1FFFFFFF))
                        .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (lang == AppLanguage.PL) "GRACZE: ${state.players.size}" else "ИГРОКОВ: ${state.players.size}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // --- Uniform Architectural Package Cards Carousel ---
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
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
            var isBackFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .height(52.dp)
                    .scale(if (isBackFocused) 1.03f else 1.0f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161B26))
                    .border(
                        width = if (isBackFocused) 2.dp else 1.dp,
                        color = if (isBackFocused) BorderFocused else BorderLight,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !isBackFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        isBackFocused = focusState.isFocused
                    }
                    .clickable {
                        TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        onBackToLobby()
                    }
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.btnBackToLobby(lang),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            val canStart = currentSelectedId != null && state.players.isNotEmpty()
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            var isStartFocused by remember { mutableStateOf(false) }
            val startScale by animateFloatAsState(
                targetValue = if (isStartFocused) 1.03f else 1.0f,
                animationSpec = tween(durationMillis = 180),
                label = "btnStartQuizScale"
            )

            Box(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .height(52.dp)
                    .scale(startScale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (canStart) Brush.horizontalGradient(listOf(NeonEmerald, Color(0xFF047857)))
                        else Brush.horizontalGradient(listOf(Color(0xFF1E2638), Color(0xFF161B26)))
                    )
                    .border(
                        width = if (isStartFocused) 2.dp else 1.dp,
                        color = if (isStartFocused) Color.White else BorderLight,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !isStartFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        isStartFocused = focusState.isFocused
                    }
                    .clickable {
                        if (canStart && currentSelectedId != null) {
                            TvSoundManager.play(TvSoundManager.SoundEffect.CORRECT)
                            onStartGame(currentSelectedId, state.selectedMode)
                        }
                    }
                    .padding(horizontal = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.btnStartQuizNow(lang),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
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
        targetValue = if (isFocused) 1.03f else 1.0f,
        animationSpec = tween(durationMillis = 160),
        label = "themeCardScale"
    )

    val packAccent = remember(pack.accentColor) {
        try {
            Color(android.graphics.Color.parseColor(pack.accentColor))
        } catch (_: Exception) {
            NeonCyan
        }
    }

    val categoryTag = remember(pack.id, pack.category, lang) {
        when {
            pack.id.contains("chgk") || pack.category.equals("ChGK", ignoreCase = true) ->
                if (lang == AppLanguage.PL) "KLUB ЧГК" else "КЛУБ ЧГК"
            pack.id.contains("svoya") || pack.category.equals("SvoyaIgra", ignoreCase = true) ->
                if (lang == AppLanguage.PL) "VA BANQUE" else "СВОЯ ИГРА"
            pack.id.contains("hard") || pack.category.equals("Masters", ignoreCase = true) ->
                if (lang == AppLanguage.PL) "EKSPERT" else "МАСТЕРА"
            pack.id.contains("millionaire") ->
                if (lang == AppLanguage.PL) "MILIONERZY" else "МИЛЛИОНЕР"
            pack.id.contains("cinema") || pack.category.equals("Culture", ignoreCase = true) ->
                if (lang == AppLanguage.PL) "KULTURA + AUDIO" else "КУЛЬТУРА + АУДИО"
            pack.id.contains("science") || pack.category.equals("Science", ignoreCase = true) ->
                if (lang == AppLanguage.PL) "NAUKA I ŚWIAT" else "НАУКА И МИР"
            pack.id.contains("encyclopedia") || pack.category.equals("Nature", ignoreCase = true) ->
                if (lang == AppLanguage.PL) "PRZYRODA" else "ЭНЦИКЛОПЕДИЯ"
            pack.id.contains("stories") || pack.category.equals("Stories", ignoreCase = true) ->
                if (lang == AppLanguage.PL) "BAJKI I KSIĄŻKI" else "СКАЗКИ И КНИГИ"
            pack.id.contains("random") || pack.category.equals("Mix", ignoreCase = true) ->
                if (lang == AppLanguage.PL) "SUPER MIX" else "СУПЕР-МИКС"
            else -> pack.category.uppercase()
        }
    }

    val defaultSec = if (pack.id.contains("chgk") || pack.category.equals("ChGK", ignoreCase = true)) 60 else 20

    Box(
        modifier = Modifier
            .width(248.dp)
            .height(264.dp)
            .scale(animatedScale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color(0xFF1A2234)
                else Color(0xFF121622)
            )
            .border(
                width = if (isFocused) 2.5.dp else if (isSelected) 2.dp else 1.dp,
                color = if (isFocused) BorderFocused else if (isSelected) packAccent else Color(0x1FFFFFFF),
                shape = RoundedCornerShape(16.dp)
            )
            .onFocusChanged { focusState ->
                if (focusState.isFocused && !isFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                isFocused = focusState.isFocused
            }
            .clickable {
                TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                onSelect()
            }
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Architectural Category Tag & Active Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(packAccent.copy(alpha = 0.16f))
                        .border(1.dp, packAccent.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = categoryTag,
                        color = packAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonCyan.copy(alpha = 0.2f))
                            .border(1.dp, NeonCyan, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = Strings.selectedThemeBadge(lang),
                            color = NeonCyan,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // Title & Description
            Column {
                Text(
                    text = pack.localizedTitle(lang),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = pack.localizedDescription(lang),
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom Metadata Row: Question Count & Default Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x14FFFFFF))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${pack.questions.size} ${if (lang == AppLanguage.PL) "PYTAŃ" else "ВОПР."}",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }

                Text(
                    text = "$defaultSec ${Strings.secondsUnit(lang)}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}
