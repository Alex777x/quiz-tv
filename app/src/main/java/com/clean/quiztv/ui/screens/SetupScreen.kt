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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import com.clean.quiztv.domain.model.GameFormat
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.Strings
import com.clean.quiztv.ui.components.DoubleBezelCard
import com.clean.quiztv.ui.theme.BorderFocused
import com.clean.quiztv.ui.theme.BorderLight
import com.clean.quiztv.ui.theme.NeonAmber
import com.clean.quiztv.ui.theme.NeonCyan
import com.clean.quiztv.ui.theme.NeonEmerald
import com.clean.quiztv.ui.theme.NeonViolet
import com.clean.quiztv.ui.theme.TextMuted

@Composable
fun SetupScreen(
    currentLanguage: AppLanguage,
    selectedMode: GameMode,
    selectedFormat: GameFormat,
    selectedRoundDuration: Int,
    onLanguageSelect: (AppLanguage) -> Unit,
    onModeSelect: (GameMode) -> Unit,
    onFormatSelect: (GameFormat) -> Unit,
    onRoundDurationSelect: (Int) -> Unit,
    onProceedToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = currentLanguage

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Section: Broadcast Editorial Style ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x1F38BDF8))
                    .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "QUIZ TV • STUDIO EDITION",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = Strings.setupTitle(lang),
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = Strings.setupSubtitle(lang),
                color = TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // --- 4 Equal-Height Architectural Parameter Columns ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(236.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Language Card
            SetupSectionCard(
                stepLabel = Strings.langStepLabel(lang),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                SetupOptionButton(
                    title = "Русский",
                    subtitle = "RU",
                    isSelected = lang == AppLanguage.RU,
                    accentColor = NeonCyan,
                    onClick = { onLanguageSelect(AppLanguage.RU) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SetupOptionButton(
                    title = "Polski",
                    subtitle = "PL",
                    isSelected = lang == AppLanguage.PL,
                    accentColor = NeonCyan,
                    onClick = { onLanguageSelect(AppLanguage.PL) }
                )
            }

            // 2. Audience / Mode Card
            SetupSectionCard(
                stepLabel = Strings.modeStepLabel(lang),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                SetupOptionButton(
                    title = if (lang == AppLanguage.PL) "Dorośli" else "Взрослые",
                    subtitle = "16+",
                    isSelected = selectedMode == GameMode.ADULTS,
                    accentColor = NeonCyan,
                    onClick = { onModeSelect(GameMode.ADULTS) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SetupOptionButton(
                    title = if (lang == AppLanguage.PL) "Dzieci i Rodzina" else "Дети и Семья",
                    subtitle = "6+",
                    isSelected = selectedMode == GameMode.KIDS,
                    accentColor = NeonAmber,
                    onClick = { onModeSelect(GameMode.KIDS) }
                )
            }

            // 3. Format Card (Solo / Teams)
            SetupSectionCard(
                stepLabel = Strings.formatStepLabel(lang),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                SetupOptionButton(
                    title = if (lang == AppLanguage.PL) "Indywidualny" else "Индивидуальный",
                    subtitle = "SOLO",
                    isSelected = selectedFormat == GameFormat.INDIVIDUAL,
                    accentColor = NeonEmerald,
                    onClick = { onFormatSelect(GameFormat.INDIVIDUAL) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SetupOptionButton(
                    title = if (lang == AppLanguage.PL) "Drużynowy" else "Командный",
                    subtitle = "TEAMS",
                    isSelected = selectedFormat == GameFormat.TEAMS,
                    accentColor = NeonViolet,
                    onClick = { onFormatSelect(GameFormat.TEAMS) }
                )
            }

            // 4. Round Timer Card (Standard Blitz vs ChGK / Discussion)
            SetupSectionCard(
                stepLabel = Strings.timerStepLabel(lang),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val isFastSelected = selectedRoundDuration <= 25
                val fastLabel = if (selectedRoundDuration == 15) {
                    if (lang == AppLanguage.PL) "Bieg (15 sek)" else "Блиц (15 сек)"
                } else {
                    if (lang == AppLanguage.PL) "Standard (20 sek)" else "Стандарт (20 сек)"
                }
                SetupOptionButton(
                    title = fastLabel,
                    subtitle = "${if (selectedRoundDuration <= 25) selectedRoundDuration else 20}S",
                    isSelected = isFastSelected,
                    accentColor = NeonEmerald,
                    onClick = {
                        val next = if (selectedRoundDuration == 20) 15 else 20
                        onRoundDurationSelect(next)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                val isChgkSelected = selectedRoundDuration >= 30
                val discSeconds = if (selectedRoundDuration >= 30) selectedRoundDuration else 60
                val discLabel = when (discSeconds) {
                    30 -> if (lang == AppLanguage.PL) "Drużyny (30 sek)" else "Команды (30 сек)"
                    45 -> if (lang == AppLanguage.PL) "Dyskusja (45 sek)" else "Дискуссия (45 сек)"
                    else -> if (lang == AppLanguage.PL) "Klub ЧГК (60 sek)" else "Клуб ЧГК (60 сек)"
                }
                SetupOptionButton(
                    title = discLabel,
                    subtitle = "${discSeconds}S",
                    isSelected = isChgkSelected,
                    accentColor = NeonAmber,
                    onClick = {
                        val next = when (selectedRoundDuration) {
                            60 -> 45
                            45 -> 30
                            else -> 60
                        }
                        onRoundDurationSelect(next)
                    }
                )
            }
        }

        // --- Bottom Primary Action Button ---
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        var isProceedFocused by remember { mutableStateOf(false) }
        val proceedScale by animateFloatAsState(
            targetValue = if (isProceedFocused) 1.03f else 1.0f,
            animationSpec = tween(durationMillis = 180),
            label = "proceedScale"
        )

        Box(
            modifier = Modifier
                .focusRequester(focusRequester)
                .width(420.dp)
                .height(58.dp)
                .scale(proceedScale)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isProceedFocused) Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF0284C7)))
                    else Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1)))
                )
                .border(
                    width = if (isProceedFocused) 2.dp else 1.dp,
                    color = if (isProceedFocused) Color.White else BorderLight,
                    shape = RoundedCornerShape(14.dp)
                )
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !isProceedFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                    isProceedFocused = focusState.isFocused
                }
                .focusable()
                .clickable {
                    TvSoundManager.play(TvSoundManager.SoundEffect.CORRECT)
                    onProceedToLobby()
                }
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.btnProceedToLobby(lang),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33000000))
                        .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupSectionCard(
    stepLabel: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    DoubleBezelCard(
        modifier = modifier,
        outerShape = RoundedCornerShape(18.dp),
        innerShape = RoundedCornerShape(16.dp),
        outerPadding = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stepLabel,
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

/**
 * Strictly uniform-height (58.dp) option control.
 * Never shifts height or wraps text when selected/unselected.
 */
@Composable
private fun SetupOptionButton(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color = NeonCyan,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 140),
        label = "btnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .scale(animatedScale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color(0xFF1E2638)
                else Color(0xFF111520)
            )
            .border(
                width = if (isFocused) 2.dp else if (isSelected) 1.5.dp else 1.dp,
                color = if (isFocused) BorderFocused else if (isSelected) accentColor else Color(0x1FFFFFFF),
                shape = RoundedCornerShape(12.dp)
            )
            .onFocusChanged { focusState ->
                if (focusState.isFocused && !isFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                isFocused = focusState.isFocused
            }
            .focusable()
            .clickable {
                TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                onClick()
            }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (isSelected) Color.White else TextMuted,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = subtitle,
                    color = if (isSelected) accentColor else TextMuted.copy(alpha = 0.55f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Fixed-size 18.dp radio indicator (identical outer footprint whether selected or unselected)
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.22f)
                            else Color.Transparent
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) accentColor else Color(0x38FFFFFF),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                    }
                }
            }
        }
    }
}
