package com.util.rsvp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.util.rsvp.component.Content
import com.util.rsvp.component.Footer

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeState,
) {

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
        content = {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2
                val wordCenterY = ((size.height / 2) - 128)
                val footerTopY = size.height * 0.7f
                val characterGapPx = 56.dp.toPx()
                val halfGap = characterGapPx / 2f

                val topEndY = (wordCenterY - halfGap).coerceAtLeast(0f)
                val bottomStartY = ((wordCenterY + halfGap).coerceAtMost(footerTopY) + 256)

                drawLine(
                    color = primaryColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, topEndY),
                    strokeWidth = 2f
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(centerX, bottomStartY),
                    end = Offset(centerX, footerTopY),
                    strokeWidth = 2f
                )
            }
            Content(
                modifier = Modifier.fillMaxWidth(),
                text = state.currentWord
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.15f)
                    .blur(72.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.primary.copy(alpha = if (isSystemInDarkTheme()) 0.10f else 0.15F),
                            )
                        )
                    )
            )
            Footer(
                modifier = Modifier.align(alignment = Alignment.BottomCenter),
                offset = state.offset,
                count = state.count,
                progress = state.progress,
                tempo = state.tempo,
                isPlay = state.isPlay,
                onSeek = state::seek,
                onPlay = state::setPlay,
                onRewin = state::rewind,
                onForward = state::forward,
                speedUp = state::speedUp,
                speedDown = state::speedDown,
            )
        }
    )
}