package com.util.rsvp.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun Footer(
    modifier: Modifier = Modifier,
    offset: Long,
    count: Long,
    progress: Long,
    tempo: Long,
    isPlay: Boolean,
    onSeek: (Long) -> Unit,
    onPlay: (Boolean) -> Unit,
    onForward: () -> Unit,
    onRewin: () -> Unit,
    speedUp: () -> Unit,
    speedDown: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(.30F),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(size = 32.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(size = 32.dp)
                    )
                    .padding(vertical = 16.dp, horizontal = 32.dp),
                content = {
                    TimeLine(
                        modifier = modifier.weight(weight = .2F),
                        offset = offset,
                        count = count,
                        progress = progress,
                        onSeek = onSeek
                    )
                    Actions(
                        modifier = modifier.weight(weight = .6F),
                        isPlay = isPlay,
                        onPlay = onPlay,
                        onRewin = onRewin,
                        onForward = onForward
                    )
                    Controller(
                        modifier = Modifier.weight(weight = .2F),
                        tempo = tempo,
                        speedUp = speedUp,
                        speedDown = speedDown)
                }
            )
        }
    )
}

@Composable
private fun TimeLine(
    modifier: Modifier = Modifier,
    offset: Long,
    count: Long,
    progress: Long,
    onSeek: (Long) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                content = {
                    SeekBar(
                        modifier = Modifier.fillMaxWidth(),
                        current = (offset + 1),
                        duration = count,
                        onSeek = onSeek
                    )
                    TimeInformation(
                        count = count,
                        offset = offset,
                        progress = progress
                    )
                }
            )
        }
    )
}

@Composable
private fun SeekBar(
    modifier: Modifier = Modifier,
    current: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {

    val progress = if (duration > 0) {
        current.toFloat() / duration.toFloat()
    } else 0f

    Slider(
        modifier = modifier,
        value = progress,
        onValueChange = {
            val newPosition = (it * duration).toLong()
            onSeek(newPosition)
        },
        valueRange = 0f..1f,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.onSurface,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            activeTickColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun TimeInformation(
    modifier: Modifier = Modifier,
    offset: Long,
    count: Long,
    progress: Long,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        content = {
            TimeLabel(
                modifier = Modifier.align(alignment = Alignment.CenterStart),
                text = "${offset + 1}/${count}"
            )
            TimeLabel(
                modifier = Modifier.align(alignment = Alignment.CenterEnd),
                text = "${progress}%"
            )
        }
    )
}

@Composable
private fun TimeLabel(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 14.sp
    )
}

@Composable
private fun Actions(
    modifier: Modifier = Modifier,
    isPlay: Boolean,
    onPlay: (Boolean) -> Unit,
    onForward: () -> Unit,
    onRewin: () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    space = 32.dp,
                    alignment = Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    Icon(
                        modifier = Modifier
                            .size(size = 32.dp)
                            .clickable {
                                onRewin()
                            },
                        imageVector = Icons.Rounded.FastRewind,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = ""
                    )

                    Icon(
                        modifier = Modifier
                            .size(size = 32.dp)
                            .clickable {
                                onPlay(isPlay.not())
                            },
                        imageVector = if (isPlay) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = ""
                    )

                    Icon(
                        modifier = Modifier
                            .size(size = 32.dp)
                            .clickable {
                                onForward()
                            },
                        imageVector = Icons.Rounded.FastForward,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = ""
                    )
                }
            )
        }
    )
}

@Composable
private fun Controller(
    modifier: Modifier = Modifier,
    tempo: Long,
    speedUp: () -> Unit,
    speedDown: () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
        content = {
            Icon(
                modifier = Modifier
                    .size(size = 24.dp)
                    .align(alignment = Alignment.CenterStart),
                imageVector = Icons.Rounded.Tune,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = ""
            )
            Metronome(
                tempo = tempo,
                speedUp = speedUp,
                speedDown = speedDown
            )
        }
    )
}

@Composable
private fun Metronome(
    modifier: Modifier = Modifier,
    tempo: Long,
    speedUp: () -> Unit,
    speedDown: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically,
        content = {
            Icon(
                modifier = Modifier
                    .size(size = 24.dp)
                    .clickable {
                        speedDown()
                    },
                imageVector = Icons.Rounded.Remove,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = ""
            )
            MetronomeLabel(tempo = tempo)
            Icon(
                modifier = Modifier
                    .size(size = 24.dp)
                    .clickable {
                        speedUp()
                    },
                imageVector = Icons.Rounded.Add,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = ""
            )
        }
    )
}

@Composable
private fun MetronomeLabel(
    modifier: Modifier = Modifier,
    tempo: Long,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            space = 6.dp,
            alignment = Alignment.CenterHorizontally
        ),
        content = {
            Text(
                text = "$tempo",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "WPM",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}