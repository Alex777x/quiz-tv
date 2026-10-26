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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clean.quiztv.audio.TvSoundManager
import com.clean.quiztv.domain.model.AppLanguage
import com.clean.quiztv.domain.model.GameFormat
import com.clean.quiztv.domain.model.PlayerStanding
import com.clean.quiztv.domain.model.Strings
import com.clean.quiztv.domain.model.TeamStanding
import com.clean.quiztv.game.GameState
import com.clean.quiztv.ui.theme.BorderFocused
import com.clean.quiztv.ui.theme.BorderLight
import com.clean.quiztv.ui.theme.BronzeMedal
import com.clean.quiztv.ui.theme.CardDark
import com.clean.quiztv.ui.theme.GoldMedal
import com.clean.quiztv.ui.theme.NeonAmber
import com.clean.quiztv.ui.theme.NeonCyan
import com.clean.quiztv.ui.theme.NeonEmerald
import com.clean.quiztv.ui.theme.SilverMedal
import com.clean.quiztv.ui.theme.TextMuted

@Composable
fun LeaderboardScreen(
    state: GameState.Leaderboard,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = state.language

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Strings.leaderboardTitle(lang),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = Strings.roundOf(state.questionIndex + 1, state.pack.questions.size, lang),
                    color = TextMuted,
                    fontSize = 16.sp
                )
            }

            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            var isFocused by remember { mutableStateOf(false) }
            val animatedScale by animateFloatAsState(
                targetValue = if (isFocused) 1.08f else 1.0f,
                animationSpec = tween(durationMillis = 200),
                label = "leaderboardProceedScale"
            )

            // Island CTA Button
            Box(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .scale(animatedScale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF0284C7)))
                    )
                    .border(
                        width = if (isFocused) 2.5.dp else 1.dp,
                        color = if (isFocused) BorderFocused else BorderLight,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .onFocusChanged {
                        if (it.isFocused && !isFocused) TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        isFocused = it.isFocused
                    }
                    .focusable()
                    .clickable {
                        TvSoundManager.play(TvSoundManager.SoundEffect.FOCUS_MOVE)
                        onProceed()
                    }
                    .padding(horizontal = 24.dp, vertical = 13.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (state.isLastQuestion) Strings.toPodiumBtn(lang) else Strings.nextQuestionBtn(lang),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x33000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.isLastQuestion) "🏆" else "➔",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Team Standings Banner (in TEAMS format)
        if (state.gameFormat == GameFormat.TEAMS && state.teamStandings.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.teamStandings.forEach { team ->
                    val color = remember(team.colorHex) {
                        try {
                            Color(android.graphics.Color.parseColor(team.colorHex))
                        } catch (_: Exception) {
                            Color(0xFF3B82F6)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(color.copy(alpha = 0.16f))
                            .border(1.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = team.emoji, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = team.name,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (team.rank == 1) "👑 #1" else "#${team.rank}",
                                        color = color,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Text(
                                text = Strings.pointsSuffix(team.totalScore, lang),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Standings List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.standings, key = { it.playerId }) { standing ->
                StandingRow(standing = standing, lang = lang)
            }
        }
    }
}

@Composable
private fun StandingRow(standing: PlayerStanding, lang: AppLanguage) {
    val rankColor = when (standing.rank) {
        1 -> GoldMedal
        2 -> SilverMedal
        3 -> BronzeMedal
        else -> TextMuted
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x18FFFFFF))
            .border(
                1.dp,
                if (standing.rank == 1) GoldMedal.copy(alpha = 0.55f) else Color(0x26FFFFFF),
                RoundedCornerShape(20.dp)
            )
            .padding(2.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(17.dp))
                .background(
                    if (standing.rank == 1) {
                        Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0x33F59E0B)))
                    } else {
                        Brush.horizontalGradient(listOf(CardDark, Color(0xFF0F172A)))
                    }
                )
                .padding(horizontal = 22.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${standing.rank}",
                    color = rankColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(48.dp)
                )

                Text(
                    text = standing.avatar,
                    fontSize = 30.sp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = standing.name,
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                if (standing.streak > 1) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NeonAmber.copy(alpha = 0.22f))
                            .border(1.dp, NeonAmber.copy(alpha = 0.45f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🔥 x${standing.streak}",
                            color = NeonAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (standing.lastScoreDiff > 0) {
                    Text(
                        text = "+${standing.lastScoreDiff}",
                        color = NeonEmerald,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                }

                Text(
                    text = Strings.pointsSuffix(standing.score, lang),
                    color = NeonCyan,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
