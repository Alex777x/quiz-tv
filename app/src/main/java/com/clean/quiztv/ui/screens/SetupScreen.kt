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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    onLanguageSelect: (AppLanguage) -> Unit,
    onModeSelect: (GameMode) -> Unit,
    onFormatSelect: (GameFormat) -> Unit,
    onProceedToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = currentLanguage

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 64.dp, vertical = 38.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Section: Title Only, Zero Clutter ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(NeonCyan, NeonViolet)))
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "QUIZ TV",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Strings.setupTitle(lang),
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }

        // --- 3 Interactive Parameter Cards ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // 1. Language Card
            SetupSectionCard(
                title = if (lang == AppLanguage.PL) "JĘZYK" else "ЯЗЫК",
                modifier = Modifier.weight(1f)
            ) {
                SetupOptionButton(
                    title = "🇵🇱 Polski",
                    isSelected = lang == AppLanguage.PL,
                    onClick = { onLanguageSelect(AppLanguage.PL) }
                )
                Spacer(modifier = Modifier.height(14.dp))
                SetupOptionButton(
                    title = "🇷🇺 Русский",
                    isSelected = lang == AppLanguage.RU,
                    onClick = { onLanguageSelect(AppLanguage.RU) }
                )
            }

            // 2. Mode Card (Adults / Kids)
            SetupSectionCard(
                title = if (lang == AppLanguage.PL) "KATEGORIA" else "КАТЕГОРИЯ",
                modifier = Modifier.weight(1f)
            ) {
                SetupOptionButton(
                    title = if (lang == AppLanguage.PL) "🍸 Dorośli" else "🍸 Взрослые",
                    isSelected = selectedMode == GameMode.ADULTS,
                    accentColor = NeonCyan,
                    onClick = { onModeSelect(GameMode.ADULTS) }
                )
                Spacer(modifier = Modifier.height(14.dp))
                SetupOptionButton(
                    title = if (lang == AppLanguage.PL) "🎈 Dzieci" else "🎈 Дети",
                    isSelected = selectedMode == GameMode.KIDS,
                    accentColor = NeonAmber,
                    onClick = { onModeSelect(GameMode.KIDS) }
                )
            }

            // 3. Format Card (Solo / Teams)
            SetupSectionCard(
                title = if (lang == AppLanguage.PL) "FORMAT" else "ФОРМАТ",
                modifier = Modifier.weight(1f)
            ) {
                SetupOptionButton(
                    title = if (lang == AppLanguage.PL) "👤 Solo" else "👤 Соло",
                    isSelected = selectedFormat == GameFormat.INDIVIDUAL,
                    accentColor = Color(0xFF60A5FA),
                    onClick = { onFormatSelect(GameFormat.INDIVIDUAL) }
                )
                Spacer(modifier = Modifier.height(14.dp))
                SetupOptionButton(
                    title = if (lang == AppLanguage.PL) "👥 Drużyny" else "👥 Команды",
                    isSelected = selectedFormat == GameFormat.TEAMS,
                    accentColor = Color(0xFFF43F5E),
                    onClick = { onFormatSelect(GameFormat.TEAMS) }
                )
            }
        }

        // --- Bottom Action Button: Proceed to Lobby ---
        var isProceedFocused by remember { mutableStateOf(false) }
        val proceedScale by animateFloatAsState(
            targetValue = if (isProceedFocused) 1.05f else 1.0f,
            animationSpec = tween(durationMillis = 200),
            label = "proceedScale"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .scale(proceedScale)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.horizontalGradient(listOf(NeonEmerald, Color(0xFF0284C7)))
                )
                .border(
                    width = if (isProceedFocused) 3.dp else 1.dp,
                    color = if (isProceedFocused) BorderFocused else BorderLight,
                    shape = RoundedCornerShape(22.dp)
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
                .padding(vertical = 16.dp, horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Strings.btnProceedToLobby(lang),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun SetupSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    DoubleBezelCard(
        modifier = modifier,
        outerShape = RoundedCornerShape(22.dp),
        innerShape = RoundedCornerShape(19.dp),
        outerPadding = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Text(
                text = title,
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            content()
        }
    }
}

@Composable
private fun SetupOptionButton(
    title: String,
    isSelected: Boolean,
    accentColor: Color = NeonCyan,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isFocused) 1.04f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "btnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Brush.horizontalGradient(
                    listOf(Color(0xFF1E293B), Color(0xFF334155))
                ) else Brush.horizontalGradient(
                    listOf(Color(0x330F172A), Color(0x330F172A))
                )
            )
            .border(
                width = if (isFocused) 2.5.dp else if (isSelected) 2.dp else 0.5.dp,
                color = if (isFocused) BorderFocused else if (isSelected) accentColor else Color(0x22FFFFFF),
                shape = RoundedCornerShape(16.dp)
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
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = if (isSelected) Color.White else TextMuted,
                fontSize = 18.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.25f))
                        .border(1.dp, accentColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "✓",
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
