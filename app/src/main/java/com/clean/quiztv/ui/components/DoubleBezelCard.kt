package com.clean.quiztv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.clean.quiztv.ui.theme.BorderLight

/**
 * Double-Bezel (Doppelrand) hardware container embodying high-end agency design.
 * Provides an outer shell with hairline specular ring and an inner tactile core.
 */
@Composable
fun DoubleBezelCard(
    modifier: Modifier = Modifier,
    outerShape: Shape = RoundedCornerShape(24.dp),
    innerShape: Shape = RoundedCornerShape(21.dp),
    outerBorderColor: Color = BorderLight,
    outerPadding: Dp = 3.dp,
    innerBrush: Brush = Brush.verticalGradient(
        listOf(
            Color(0xEE1E293B),
            Color(0xF50F172A)
        )
    ),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(outerShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0x1CFFFFFF),
                        Color(0x08FFFFFF)
                    )
                )
            )
            .border(1.dp, outerBorderColor, outerShape)
            .padding(outerPadding)
    ) {
        Box(
            modifier = Modifier
                .clip(innerShape)
                .background(innerBrush)
                .border(0.5.dp, Color(0x24FFFFFF), innerShape)
        ) {
            content()
        }
    }
}
