package com.util.rsvp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.util.rsvp.component.Content
import com.util.rsvp.component.Footer
import kotlinx.coroutines.delay

private const val LOREM =
    """Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of "de Finibus Bonorum et Malorum" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, "Lorem ipsum dolor sit amet..", comes from a line in section 1.10.32.
    
The standard chunk of Lorem Ipsum used since the 1500s is reproduced below for those interested. Sections 1.10.32 and 1.10.33 from "de Finibus Bonorum et Malorum" by Cicero are also reproduced in their exact original form, accompanied by English versions from the 1914 translation by H. Rackham."""

private const val TIMELINE_MOVE_STEP = 10L
private const val SPEED_STEP = 10L

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {

    val stack = remember {
        LOREM
            .split(" ")
            .map { it.trim() }
    }
    var currentWord by remember { mutableStateOf(value = stack.first()) }
    var tempo by remember { mutableStateOf(value = 60L) }

    var offset by remember { mutableStateOf(value = 0L) }
    val count = remember { stack.size.toLong() }
    var progress by remember {
        mutableStateOf(value = 0L)
    }
    var isPlay by remember { mutableStateOf(value = false) }

    LaunchedEffect(key1 = isPlay) {
        if (offset >= count || !isPlay) return@LaunchedEffect

        while (offset < count - 1) {
            offset++
            currentWord = stack[offset.toInt()]
            delay(timeMillis = ((60.0 / tempo.coerceAtLeast(1)) * 1000).toLong().coerceAtLeast(1))
        }
    }

    LaunchedEffect(key1 = offset) {
        val index = offset.coerceIn(0, count - 1).toInt()
        currentWord = stack[index]
        progress = if (count > 0) ((offset * 100) / count) else 0
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
        content = {
            Content(word = currentWord)
            Footer(
                modifier = Modifier.align(alignment = Alignment.BottomCenter),
                offset = offset,
                count = count,
                progress = progress,
                tempo = tempo,
                isPlay = isPlay,
                onSeek = {
                    offset = it
                },
                onPlay = {
                    isPlay = it
                },
                onRewin = {
                    offset =
                        if ((offset - TIMELINE_MOVE_STEP) <= 0) 0 else (offset - TIMELINE_MOVE_STEP)
                },
                onForward = {
                    offset =
                        if ((offset + TIMELINE_MOVE_STEP) >= (count - 1)) (count - 1) else (offset + TIMELINE_MOVE_STEP)
                },
                speedUp = {
                    tempo += SPEED_STEP
                },
                speedDown = {
                    tempo -= SPEED_STEP
                }
            )
        }
    )
}