package com.clean.quiztv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.clean.quiztv.domain.model.Player
import com.clean.quiztv.domain.model.Strings
import com.clean.quiztv.game.GameState
import com.clean.quiztv.ui.components.DoubleBezelCard
import com.clean.quiztv.ui.theme.AnswerBlue
import com.clean.quiztv.ui.theme.AnswerGreen
import com.clean.quiztv.ui.theme.AnswerRed
import com.clean.quiztv.ui.theme.AnswerYellow
import com.clean.quiztv.ui.theme.NeonCyan
import com.clean.quiztv.ui.theme.NeonEmerald
import com.clean.quiztv.ui.theme.TextMuted

@Composable
fun RevealScreen(
    state: GameState.Reveal,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = state.language
    val correctIdx = state.question.correctIndex
    val funFact = state.question.localizedFunFact(lang)

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
            .padding(horizontal = 48.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Question title & visual media (Double-Bezel)
        DoubleBezelCard(
            modifier = Modifier.fillMaxWidth(),
            outerShape = RoundedCornerShape(22.dp),
            innerShape = RoundedCornerShape(19.dp),
            outerPadding = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Image thumbnail if question had an image
                if (imageModel != null) {
                    Box(
                        modifier = Modifier
                            .size(width = 160.dp, height = 100.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(14.dp)),
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
                    Spacer(modifier = Modifier.width(20.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Visual Melody Synthesizer Banner
                    if (state.question.melodyTune != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33A855F7))
                                .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = Strings.musicGuessTitle(lang),
                                color = Color(0xFFD8B4FE),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Visual Rebus Emoji Box
                    if (state.question.rebusEmoji != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.radialGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))))
                                .border(1.5.dp, NeonCyan, RoundedCornerShape(16.dp))
                                .padding(horizontal = 22.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = state.question.rebusEmoji,
                                fontSize = 36.sp,
                                letterSpacing = 3.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = state.question.localizedText(lang),
                        color = Color.White,
                        fontSize = if (imageModel != null || state.question.melodyTune != null || state.question.rebusEmoji != null) 21.sp else 23.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Fun Fact Card (Double-Bezel with radiant cyan highlight)
        if (funFact.isNotEmpty()) {
            DoubleBezelCard(
                modifier = Modifier.fillMaxWidth(),
                outerShape = RoundedCornerShape(20.dp),
                innerShape = RoundedCornerShape(17.dp),
                outerBorderColor = NeonCyan.copy(alpha = 0.45f),
                innerBrush = Brush.horizontalGradient(
                    listOf(
                        NeonCyan.copy(alpha = 0.16f),
                        Color(0xF50F172A)
                    )
                ),
                outerPadding = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "💡", fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = Strings.funFactTitle(lang),
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = funFact,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        // 4 Options with Player Avatar Badges Underneath
        val colors = listOf(AnswerRed, AnswerBlue, AnswerYellow, AnswerGreen)
        val symbols = listOf("▲", "◆", "●", "■")
        val localizedOpts = state.question.localizedOptions(lang)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RevealOptionCard(
                    symbol = symbols[0],
                    text = localizedOpts.getOrElse(0) { "" },
                    color = colors[0],
                    isCorrect = correctIdx == 0,
                    votesCount = state.optionCounts[0] ?: 0,
                    pickedPlayers = state.players.filter { it.lastAnswer?.optionIndex == 0 },
                    modifier = Modifier.weight(1f)
                )
                RevealOptionCard(
                    symbol = symbols[1],
                    text = localizedOpts.getOrElse(1) { "" },
                    color = colors[1],
                    isCorrect = correctIdx == 1,
                    votesCount = state.optionCounts[1] ?: 0,
                    pickedPlayers = state.players.filter { it.lastAnswer?.optionIndex == 1 },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RevealOptionCard(
                    symbol = symbols[2],
                    text = localizedOpts.getOrElse(2) { "" },
                    color = colors[2],
                    isCorrect = correctIdx == 2,
                    votesCount = state.optionCounts[2] ?: 0,
                    pickedPlayers = state.players.filter { it.lastAnswer?.optionIndex == 2 },
                    modifier = Modifier.weight(1f)
                )
                RevealOptionCard(
                    symbol = symbols[3],
                    text = localizedOpts.getOrElse(3) { "" },
                    color = colors[3],
                    isCorrect = correctIdx == 3,
                    votesCount = state.optionCounts[3] ?: 0,
                    pickedPlayers = state.players.filter { it.lastAnswer?.optionIndex == 3 },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RevealOptionCard(
    symbol: String,
    text: String,
    color: Color,
    isCorrect: Boolean,
    votesCount: Int,
    pickedPlayers: List<Player>,
    modifier: Modifier = Modifier
) {
    val alpha = if (isCorrect) 1.0f else 0.35f

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha)
                .height(76.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isCorrect) NeonEmerald.copy(alpha = 0.3f) else Color(0x18FFFFFF))
                .border(
                    width = if (isCorrect) 2.5.dp else 1.dp,
                    color = if (isCorrect) Color.White else Color(0x33FFFFFF),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(2.5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(17.dp))
                    .background(if (isCorrect) NeonEmerald else color)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x33000000))
                            .border(1.dp, Color(0x40FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCorrect) "✓" else symbol,
                            color = Color.White,
                            fontSize = if (isCorrect) 21.sp else 19.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (votesCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x33000000))
                            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "$votesCount 👤",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Avatar Badges under the chosen option
        if (pickedPlayers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pickedPlayers.take(4).forEach { player ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x40000000))
                            .border(0.5.dp, Color(0x40FFFFFF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${player.avatar} ${player.name}",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (pickedPlayers.size > 4) {
                    Text(
                        text = "+${pickedPlayers.size - 4}",
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
