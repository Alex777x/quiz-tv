package com.clean.quiztv.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clean.quiztv.domain.model.GameMode
import com.clean.quiztv.domain.model.Strings
import com.clean.quiztv.game.GameState
import com.clean.quiztv.ui.components.DoubleBezelCard
import com.clean.quiztv.ui.theme.AnswerBlue
import com.clean.quiztv.ui.theme.AnswerGreen
import com.clean.quiztv.ui.theme.AnswerRed
import com.clean.quiztv.ui.theme.AnswerYellow
import com.clean.quiztv.ui.theme.BorderLight
import com.clean.quiztv.ui.theme.CardDark
import com.clean.quiztv.ui.theme.NeonAmber
import com.clean.quiztv.ui.theme.NeonCyan
import com.clean.quiztv.ui.theme.TextMuted

@Composable
fun QuestionScreen(
    state: GameState.QuestionRound,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val totalTime = state.question.timeLimitSeconds.toFloat().coerceAtLeast(1f)
    val remaining = state.secondsRemaining.toFloat()
    val progress by animateFloatAsState(
        targetValue = (remaining / totalTime).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350),
        label = "timerProgress"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Top Bar: Round & Category Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = Strings.questionRound(state.questionIndex + 1, state.pack.questions.size, lang),
                        color = NeonCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = state.pack.localizedTitle(lang),
                    color = TextMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Strings.modeBadge(state.pack.ageGroup, lang),
                    color = if (state.pack.ageGroup == GameMode.KIDS) NeonAmber else NeonCyan,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(20.dp))

                // Answers received badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CardDark)
                        .border(1.dp, BorderLight, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = Strings.answersReceived(state.answersReceived, state.totalPlayers, lang),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Center: Question Text (Double-Bezel) & Circular Timer ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            DoubleBezelCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                outerShape = RoundedCornerShape(26.dp),
                innerShape = RoundedCornerShape(23.dp),
                outerPadding = 3.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Visual Melody Synthesizer Banner
                        if (state.question.melodyTune != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x33A855F7))
                                    .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = Strings.musicGuessTitle(lang),
                                    color = Color(0xFFD8B4FE),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Visual Rebus Emoji Box
                        if (state.question.rebusEmoji != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Brush.radialGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))))
                                    .border(1.5.dp, NeonCyan, RoundedCornerShape(18.dp))
                                    .padding(horizontal = 26.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = state.question.rebusEmoji,
                                    fontSize = 44.sp,
                                    letterSpacing = 4.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = state.question.localizedText(lang),
                            color = Color.White,
                            fontSize = if (state.question.melodyTune != null || state.question.rebusEmoji != null) 24.sp else 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = if (state.question.melodyTune != null || state.question.rebusEmoji != null) 33.sp else 39.sp
                        )
                    }
                }
            }

            // Big Timer Counter with glowing radial halo
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (state.secondsRemaining <= 5) AnswerRed.copy(alpha = 0.45f) else NeonCyan.copy(alpha = 0.3f),
                                CardDark
                            )
                        )
                    )
                    .border(
                        3.dp,
                        if (state.secondsRemaining <= 5) AnswerRed else NeonCyan,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.secondsRemaining}",
                        color = if (state.secondsRemaining <= 5) AnswerRed else Color.White,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = Strings.secondsUnit(lang),
                        color = TextMuted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Timer Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (state.secondsRemaining <= 5) AnswerRed else NeonCyan,
            trackColor = Color(0x2BFFFFFF)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Bottom: 4 Colored Answer Cards ---
        val colors = listOf(AnswerRed, AnswerBlue, AnswerYellow, AnswerGreen)
        val symbols = listOf("▲", "◆", "●", "■")
        val localizedOpts = state.question.localizedOptions(lang)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OptionCard(
                    symbol = symbols[0],
                    text = localizedOpts.getOrElse(0) { "" },
                    color = colors[0],
                    modifier = Modifier.weight(1f)
                )
                OptionCard(
                    symbol = symbols[1],
                    text = localizedOpts.getOrElse(1) { "" },
                    color = colors[1],
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OptionCard(
                    symbol = symbols[2],
                    text = localizedOpts.getOrElse(2) { "" },
                    color = colors[2],
                    modifier = Modifier.weight(1f)
                )
                OptionCard(
                    symbol = symbols[3],
                    text = localizedOpts.getOrElse(3) { "" },
                    color = colors[3],
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Host Pause Overlay
        if (state.isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xEE070A14)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0x33F59E0B))
                            .border(2.dp, NeonAmber, RoundedCornerShape(22.dp))
                            .padding(horizontal = 36.dp, vertical = 18.dp)
                    ) {
                        Text(
                            text = Strings.gamePausedTitle(lang),
                            color = NeonAmber,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = Strings.gamePausedSubtitle(lang),
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionCard(
    symbol: String,
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x18FFFFFF))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
            .padding(2.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(17.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(color, color.copy(alpha = 0.88f))
                    )
                )
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x33000000))
                    .border(1.dp, Color(0x40FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
