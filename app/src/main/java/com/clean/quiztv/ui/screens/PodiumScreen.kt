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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clean.quiztv.audio.TvSoundManager
import com.clean.quiztv.domain.model.AppLanguage
import com.clean.quiztv.domain.model.GameFormat
import com.clean.quiztv.domain.model.PlayerStanding
import com.clean.quiztv.domain.model.Strings
import com.clean.quiztv.game.GameState
import com.clean.quiztv.ui.theme.BorderLight
import com.clean.quiztv.ui.theme.BronzeMedal
import com.clean.quiztv.ui.theme.CardDark
import com.clean.quiztv.ui.theme.GoldMedal
import com.clean.quiztv.ui.theme.NeonCyan
import com.clean.quiztv.ui.theme.NeonEmerald
import com.clean.quiztv.ui.theme.SilverMedal

@Composable
fun PodiumScreen(
    state: GameState.Podium,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val top1 = state.standings.getOrNull(0)
    val top2 = state.standings.getOrNull(1)
    val top3 = state.standings.getOrNull(2)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val title = if (state.gameFormat == GameFormat.TEAMS && state.winningTeam != null) {
                Strings.teamWinnerTitle(state.winningTeam.name, lang)
            } else {
                Strings.quizWinnerTitle(lang)
            }
            Text(
                text = title,
                color = GoldMedal,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            val championText = if (state.gameFormat == GameFormat.TEAMS && state.winningTeam != null) {
                "${state.winningTeam.name} — ${Strings.pointsSuffix(state.winningTeam.totalScore, lang)}"
            } else {
                Strings.congratsChampion(state.winner?.name ?: "—", lang)
            }
            Text(
                text = championText,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 3-Step Architectural Podium Pedestal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 96.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            if (top2 != null) {
                PodiumColumn(
                    standing = top2,
                    place = "02",
                    pedestalHeight = 150.dp,
                    color = SilverMedal,
                    lang = lang,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.width(24.dp))

            if (top1 != null) {
                PodiumColumn(
                    standing = top1,
                    place = "01",
                    pedestalHeight = 210.dp,
                    color = GoldMedal,
                    lang = lang,
                    modifier = Modifier.weight(1.15f)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            if (top3 != null) {
                PodiumColumn(
                    standing = top3,
                    place = "03",
                    pedestalHeight = 115.dp,
                    color = BronzeMedal,
                    lang = lang,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Play Again CTA
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        var isFocused by remember { mutableStateOf(false) }
        val animatedScale by animateFloatAsState(
            targetValue = if (isFocused) 1.03f else 1.0f,
            animationSpec = tween(durationMillis = 180),
            label = "podiumPlayAgainScale"
        )

        Box(
            modifier = Modifier
                .focusRequester(focusRequester)
                .width(340.dp)
                .height(54.dp)
                .scale(animatedScale)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(listOf(NeonEmerald, Color(0xFF047857)))
                )
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) Color.White else BorderLight,
                    shape = RoundedCornerShape(14.dp)
                )
                .onFocusChanged {
                    if (it.isFocused && !isFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                    isFocused = it.isFocused
                }
                .focusable()
                .clickable {
                    TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                    onPlayAgain()
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
                    text = Strings.playAgainBtn(lang),
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
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    standing: PlayerStanding,
    place: String,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    color: Color,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val monogram = remember(standing.avatar, standing.name) {
        val av = standing.avatar.trim()
        if (av.length in 1..2 && av.all { it.isLetterOrDigit() }) av.uppercase()
        else standing.name.trim().take(2).uppercase().ifEmpty { "P" }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF161B26))
                .border(1.5.dp, color, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = monogram,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = standing.name,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = Strings.pointsSuffix(standing.score, lang),
            color = NeonCyan,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            color.copy(alpha = 0.35f),
                            CardDark
                        )
                    )
                )
                .border(
                    1.5.dp,
                    color.copy(alpha = 0.65f),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = place,
                color = color,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
