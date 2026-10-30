package com.clean.quiztv.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.clean.quiztv.audio.TvSoundManager
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
    val context = LocalContext.current
    val lang = state.language
    val isKids = state.pack.ageGroup == GameMode.KIDS
    val isChgk = state.pack.category.equals("ChGK", ignoreCase = true)
    val totalTime = remember(state.question.id) { state.secondsRemaining.toFloat().coerceAtLeast(1f) }
    val remaining = state.secondsRemaining.toFloat()
    val progress by animateFloatAsState(
        targetValue = (remaining / totalTime).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350),
        label = "timerProgress"
    )

    val imageModel = remember(state.question.imageAsset) {
        val asset = state.question.imageAsset
        when {
            asset == null -> null
            asset.startsWith("http://") || asset.startsWith("https://") -> asset
            else -> "file:///android_asset/$asset"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Top Bar: Round & Category Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val badgeColor = when {
                    isChgk -> NeonAmber
                    isKids -> Color(0xFF10B981)
                    else -> NeonCyan
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.18f))
                        .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = Strings.questionRound(state.questionIndex + 1, state.pack.questions.size, lang),
                        color = badgeColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = state.pack.localizedTitle(lang).uppercase(),
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isChgk) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonAmber.copy(alpha = 0.14f))
                            .border(1.dp, NeonAmber.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "ИНТЕЛЛЕКТУАЛЬНЫЙ КЛУБ • ЧГК",
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardDark)
                        .border(1.dp, BorderLight, RoundedCornerShape(6.dp))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = Strings.answersReceived(state.answersReceived, state.totalPlayers, lang),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        // --- Center: Question Content (Text, Optional Photo/Audio) & Circular Timer ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            DoubleBezelCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                outerShape = RoundedCornerShape(20.dp),
                innerShape = RoundedCornerShape(18.dp),
                outerPadding = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (imageModel != null) {
                        Box(
                            modifier = Modifier
                                .width(290.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0B0E17))
                                .border(1.dp, BorderLight, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imageModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Question Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(24.dp))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (state.question.melodyTune != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x26818CF8))
                                        .border(1.dp, Color(0xFF818CF8), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = Strings.musicGuessTitle(lang),
                                        color = Color(0xFFC7D2FE),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                                        .clickable { TvSoundManager.replayMelody() }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = Strings.replayMelodyBtn(lang),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        val qText = state.question.localizedText(lang)
                        val fontSize = when {
                            qText.length > 220 -> 20.sp
                            qText.length > 140 -> 22.sp
                            imageModel != null -> 23.sp
                            else -> 26.sp
                        }
                        val lineHeight = when {
                            qText.length > 220 -> 28.sp
                            qText.length > 140 -> 30.sp
                            else -> 35.sp
                        }

                        Text(
                            text = qText,
                            color = Color.White,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = lineHeight
                        )
                    }
                }
            }

            // Broadcast Timer Counter
            val timerAccent = if (state.secondsRemaining <= 5) Color(0xFFEF4444) else if (isChgk) NeonAmber else NeonCyan
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                timerAccent.copy(alpha = 0.25f),
                                Color(0xFF111520)
                            )
                        )
                    )
                    .border(2.5.dp, timerAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.secondsRemaining}",
                        color = if (state.secondsRemaining <= 5) Color(0xFFEF4444) else Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = Strings.secondsUnit(lang),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        // Timer Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (state.secondsRemaining <= 5) Color(0xFFEF4444) else if (isChgk) NeonAmber else NeonCyan,
            trackColor = Color(0x1FFFFFFF)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // --- Bottom: 4 Broadcast Option Cards (A, B, C, D) ---
        val colors = listOf(AnswerRed, AnswerBlue, AnswerYellow, AnswerGreen)
        val labels = listOf("A", "B", "C", "D")
        val localizedOpts = state.question.localizedOptions(lang)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OptionCard(
                    badge = labels[0],
                    text = localizedOpts.getOrElse(0) { "" },
                    accentColor = colors[0],
                    modifier = Modifier.weight(1f)
                )
                OptionCard(
                    badge = labels[1],
                    text = localizedOpts.getOrElse(1) { "" },
                    accentColor = colors[1],
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OptionCard(
                    badge = labels[2],
                    text = localizedOpts.getOrElse(2) { "" },
                    accentColor = colors[2],
                    modifier = Modifier.weight(1f)
                )
                OptionCard(
                    badge = labels[3],
                    text = localizedOpts.getOrElse(3) { "" },
                    accentColor = colors[3],
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OptionCard(
    badge: String,
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141926))
            .border(1.5.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor)
                    .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
