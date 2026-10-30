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
import com.clean.quiztv.ui.theme.BorderLight
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
        // Question title & visual media
        DoubleBezelCard(
            modifier = Modifier.fillMaxWidth(),
            outerShape = RoundedCornerShape(18.dp),
            innerShape = RoundedCornerShape(16.dp),
            outerPadding = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (imageModel != null) {
                    Box(
                        modifier = Modifier
                            .size(width = 148.dp, height = 92.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0B0E17))
                            .border(1.dp, BorderLight, RoundedCornerShape(10.dp)),
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
                    Text(
                        text = state.question.localizedText(lang),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Commentary / Explanation Card (only shown when a genuine fact/explanation exists)
        val hasGenuineFact = funFact.isNotBlank() &&
            !funFact.lowercase().contains("правильный ответ") &&
            !funFact.lowercase().contains("prawidłowa odpowiedź")
        if (hasGenuineFact) {
            DoubleBezelCard(
                modifier = Modifier.fillMaxWidth(),
                outerShape = RoundedCornerShape(16.dp),
                innerShape = RoundedCornerShape(14.dp),
                outerBorderColor = NeonCyan.copy(alpha = 0.4f),
                innerBrush = Brush.horizontalGradient(
                    listOf(
                        NeonCyan.copy(alpha = 0.12f),
                        Color(0xF5111520)
                    )
                ),
                outerPadding = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = Strings.funFactTitle(lang),
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = funFact,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 21.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 4 Options with Player Badges Underneath
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
                RevealOptionCard(
                    badge = labels[0],
                    text = localizedOpts.getOrElse(0) { "" },
                    color = colors[0],
                    isCorrect = correctIdx == 0,
                    votesCount = state.optionCounts[0] ?: 0,
                    pickedPlayers = state.players.filter { it.lastAnswer?.optionIndex == 0 },
                    modifier = Modifier.weight(1f)
                )
                RevealOptionCard(
                    badge = labels[1],
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
                    badge = labels[2],
                    text = localizedOpts.getOrElse(2) { "" },
                    color = colors[2],
                    isCorrect = correctIdx == 2,
                    votesCount = state.optionCounts[2] ?: 0,
                    pickedPlayers = state.players.filter { it.lastAnswer?.optionIndex == 2 },
                    modifier = Modifier.weight(1f)
                )
                RevealOptionCard(
                    badge = labels[3],
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
    badge: String,
    text: String,
    color: Color,
    isCorrect: Boolean,
    votesCount: Int,
    pickedPlayers: List<Player>,
    modifier: Modifier = Modifier
) {
    val alpha = if (isCorrect) 1.0f else 0.38f

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha)
                .height(72.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isCorrect) Color(0xFF064E3B) else Color(0xFF141926))
                .border(
                    width = if (isCorrect) 2.dp else 1.dp,
                    color = if (isCorrect) NeonEmerald else BorderLight,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isCorrect) NeonEmerald else color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badge,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (votesCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x40000000))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$votesCount",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        if (pickedPlayers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pickedPlayers.take(4).forEach { player ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF161B26))
                            .border(0.5.dp, BorderLight, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = player.name,
                            color = Color.White,
                            fontSize = 11.sp,
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
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}
