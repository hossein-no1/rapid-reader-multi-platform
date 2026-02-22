package com.util.rsvp.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GuidedContent(
    modifier: Modifier = Modifier,
    text: String,
    highlightStart: Int,
    highlightEndExclusive: Int,
) {
    if (text.isEmpty()) return

    val focusedTextColor = MaterialTheme.colorScheme.onBackground
    val unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)

    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = 18.sp,
        lineHeight = 28.sp,
    )

    val safeStart = highlightStart.coerceIn(0, text.length)
    val safeEndExclusive = highlightEndExclusive.coerceIn(safeStart, text.length)

    val annotated = remember(text, safeStart, safeEndExclusive, focusedTextColor) {
        if (safeStart == safeEndExclusive) {
            AnnotatedString(text)
        } else {
            AnnotatedString(
                text = text,
                spanStyles = listOf(
                    AnnotatedString.Range(
                        item = SpanStyle(
                            color = focusedTextColor,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        start = safeStart,
                        end = safeEndExclusive,
                    )
                )
            )
        }
    }

    val scrollState = rememberScrollState()

    val maxLines = 6
    val maxHeightSp = (textStyle.lineHeight.value * maxLines).sp
    val density = LocalDensity.current
    val maxHeightDp = with(density) { maxHeightSp.toDp() }
    val maxHeightPx = with(density) { maxHeightDp.toPx() }

    val bg = MaterialTheme.colorScheme.background
    val fadeTint = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    val topFade = remember(bg, fadeTint) {
        Brush.verticalGradient(
            colors = listOf(bg, fadeTint, Color.Transparent),
        )
    }
    val bottomFade = remember(bg, fadeTint) {
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, fadeTint, bg),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 72.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        var textLayout: TextLayoutResult? by remember { mutableStateOf(null) }
        var highlightLineIndex by remember { mutableStateOf(0) }

        LaunchedEffect(textLayout, safeStart, text.length, maxHeightPx) {
            val layout = textLayout ?: return@LaunchedEffect
            if (text.isEmpty()) return@LaunchedEffect

            val offsetForBox = safeStart.coerceIn(0, (text.length - 1).coerceAtLeast(0))
            val line = layout.getLineForOffset(offsetForBox)
            highlightLineIndex = line

            val lineTop = layout.getLineTop(line)
            val lineBottom = layout.getLineBottom(line)
            val lineCenterY = (lineTop + lineBottom) / 2f

            val targetScrollPx = if (line == 0) {
                0f
            } else {
                lineCenterY - (maxHeightPx / 2f)
            }

            scrollState.animateScrollTo(
                value = targetScrollPx
                    .toInt()
                    .coerceAtLeast(0)
                    .coerceAtMost(scrollState.maxValue)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .heightIn(max = maxHeightDp)
                .clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState, enabled = false),
                text = annotated,
                color = unfocusedTextColor,
                style = textStyle,
                onTextLayout = { textLayout = it },
            )

            if (highlightLineIndex > 0 && scrollState.value > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(topFade)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(bottomFade)
            )
        }
    }
}
