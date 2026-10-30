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
 * Architectural Double-Bezel container with crisp hairline border and elevated dark slate surface.
 */
@Composable
fun DoubleBezelCard(
    modifier: Modifier = Modifier,
    outerShape: Shape = RoundedCornerShape(18.dp),
    innerShape: Shape = RoundedCornerShape(16.dp),
    outerBorderColor: Color = BorderLight,
    outerPadding: Dp = 2.dp,
    innerBrush: Brush = Brush.verticalGradient(
        listOf(
            Color(0xF2161B26),
            Color(0xFA0E121B)
        )
    ),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(outerShape)
            .background(Color(0x0DFFFFFF))
            .border(1.dp, outerBorderColor, outerShape)
            .padding(outerPadding)
    ) {
        Box(
            modifier = Modifier
                .clip(innerShape)
                .background(innerBrush)
                .border(0.5.dp, Color(0x18FFFFFF), innerShape)
        ) {
            content()
        }
    }
}
