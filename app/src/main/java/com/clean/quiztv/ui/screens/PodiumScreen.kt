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
import androidx.compose.foundation.shape.CircleShape
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
import com.clean.quiztv.domain.model.PlayerStanding
import com.clean.quiztv.domain.model.Strings
import com.clean.quiztv.game.GameState
import com.clean.quiztv.ui.theme.BorderFocused
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
            .padding(horizontal = 48.dp, vertical = 26.dp),
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
            Spacer(modifier = Modifier.height(4.dp))
            val championText = if (state.gameFormat == GameFormat.TEAMS && state.winningTeam != null) {
                "${state.winningTeam.emoji} ${state.winningTeam.name} — ${state.winningTeam.totalScore} pkt"
            } else {
                Strings.congratsChampion(state.winner?.name ?: if (lang == AppLanguage.PL) "Wszyscy" else "Дружба", lang)
            }
            Text(
                text = championText,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 3-Step Podium Pedestal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 80.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            // #2 Silver (Left)
            if (top2 != null) {
                PodiumColumn(
                    standing = top2,
                    place = "2",
                    pedestalHeight = 160.dp,
                    color = SilverMedal,
                    lang = lang,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.width(24.dp))

            // #1 Gold (Center, Highest)
            if (top1 != null) {
                PodiumColumn(
                    standing = top1,
                    place = "1",
                    pedestalHeight = 220.dp,
                    color = GoldMedal,
                    lang = lang,
                    modifier = Modifier.weight(1.15f)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // #3 Bronze (Right)
            if (top3 != null) {
                PodiumColumn(
                    standing = top3,
                    place = "3",
                    pedestalHeight = 120.dp,
                    color = BronzeMedal,
                    lang = lang,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Host Remote Button: Play Again
        var isFocused by remember { mutableStateOf(false) }
        val animatedScale by animateFloatAsState(
            targetValue = if (isFocused) 1.08f else 1.0f,
            animationSpec = tween(durationMillis = 200),
            label = "podiumPlayAgainScale"
        )

        Box(
            modifier = Modifier
                .scale(animatedScale)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.horizontalGradient(listOf(NeonEmerald, Color(0xFF047857)))
                )
                .border(
                    width = if (isFocused) 2.5.dp else 1.dp,
                    color = if (isFocused) BorderFocused else BorderLight,
                    shape = RoundedCornerShape(22.dp)
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
                .padding(horizontal = 32.dp, vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Strings.playAgainBtn(lang),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0x33000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔄",
                        color = Color.White,
                        fontSize = 14.sp
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = standing.avatar, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = standing.name,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = Strings.pointsSuffix(standing.score, lang),
            color = NeonCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Pedestal block with Double-Bezel styling
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(Color(0x22FFFFFF))
                .border(
                    1.dp,
                    color.copy(alpha = 0.5f),
                    RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                )
                .padding(top = 2.5.dp, start = 2.5.dp, end = 2.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 19.dp, topEnd = 19.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                color.copy(alpha = 0.45f),
                                CardDark
                            )
                        )
                    )
                    .border(
                        1.dp,
                        color.copy(alpha = 0.7f),
                        RoundedCornerShape(topStart = 19.dp, topEnd = 19.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = place,
                    color = color,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
