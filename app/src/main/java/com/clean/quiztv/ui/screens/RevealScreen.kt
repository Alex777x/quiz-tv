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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clean.quiztv.domain.model.Strings
import com.clean.quiztv.game.GameState
import com.clean.quiztv.ui.components.DoubleBezelCard
import com.clean.quiztv.ui.theme.AnswerBlue
import com.clean.quiztv.ui.theme.AnswerGreen
import com.clean.quiztv.ui.theme.AnswerRed
import com.clean.quiztv.ui.theme.AnswerYellow
import com.clean.quiztv.ui.theme.NeonCyan
import com.clean.quiztv.ui.theme.NeonEmerald

@Composable
fun RevealScreen(
    state: GameState.Reveal,
    modifier: Modifier = Modifier
) {
    val lang = state.language
    val correctIdx = state.question.correctIndex
    val funFact = state.question.localizedFunFact(lang)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Question title (Double-Bezel)
        DoubleBezelCard(
            modifier = Modifier.fillMaxWidth(),
            outerShape = RoundedCornerShape(22.dp),
            innerShape = RoundedCornerShape(19.dp),
            outerPadding = 3.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.question.localizedText(lang),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Fun Fact Card (Double-Bezel with radiant cyan highlight)
        if (funFact.isNotEmpty()) {
            DoubleBezelCard(
                modifier = Modifier.fillMaxWidth(),
                outerShape = RoundedCornerShape(22.dp),
                innerShape = RoundedCornerShape(19.dp),
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
                        .padding(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "💡", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = Strings.funFactTitle(lang),
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = funFact,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }

        // 4 Options
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
                RevealOptionCard(
                    symbol = symbols[0],
                    text = localizedOpts.getOrElse(0) { "" },
                    color = colors[0],
                    isCorrect = correctIdx == 0,
                    votesCount = state.optionCounts[0] ?: 0,
                    modifier = Modifier.weight(1f)
                )
                RevealOptionCard(
                    symbol = symbols[1],
                    text = localizedOpts.getOrElse(1) { "" },
                    color = colors[1],
                    isCorrect = correctIdx == 1,
                    votesCount = state.optionCounts[1] ?: 0,
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
                    modifier = Modifier.weight(1f)
                )
                RevealOptionCard(
                    symbol = symbols[3],
                    text = localizedOpts.getOrElse(3) { "" },
                    color = colors[3],
                    isCorrect = correctIdx == 3,
                    votesCount = state.optionCounts[3] ?: 0,
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
    modifier: Modifier = Modifier
) {
    val alpha = if (isCorrect) 1.0f else 0.32f

    Box(
        modifier = modifier
            .alpha(alpha)
            .height(80.dp)
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
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
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
                        text = if (isCorrect) "✓" else symbol,
                        color = Color.White,
                        fontSize = if (isCorrect) 22.sp else 20.sp,
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

            if (votesCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33000000))
                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$votesCount 👤",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
