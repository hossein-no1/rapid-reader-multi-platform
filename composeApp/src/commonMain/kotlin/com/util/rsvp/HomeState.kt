package com.util.rsvp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

private const val TIMELINE_MOVE_STEP = 10L
private const val SPEED_STEP = 10L

private const val LOREM =
    """Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of "de Finibus Bonorum et Malorum" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, "Lorem ipsum dolor sit amet..", comes from a line in section 1.10.32.
    
The standard chunk of Lorem Ipsum used since the 1500s is reproduced below for those interested. Sections 1.10.32 and 1.10.33 from "de Finibus Bonorum et Malorum" by Cicero are also reproduced in their exact original form, accompanied by English versions from the 1914 translation by H. Rackham."""

@Stable
class HomeState internal constructor(
    internal val stack: List<String>,
) {
    val count: Long = stack.size.toLong()

    var currentWord: String by mutableStateOf(value = stack.firstOrNull().orEmpty())
        internal set

    var tempo: Long by mutableStateOf(value = 60L)
        private set

    var offset: Long by mutableStateOf(value = 0L)
        private set

    var progress: Long by mutableStateOf(value = 0L)
        internal set

    var isPlay: Boolean by mutableStateOf(value = false)
        private set

    fun seek(newOffset: Long) {
        offset = newOffset.coerceIn(0, (count - 1).coerceAtLeast(0))
    }

    fun setPlay(newValue: Boolean) {
        isPlay = newValue
    }

    fun rewind() {
        seek(offset - TIMELINE_MOVE_STEP)
    }

    fun forward() {
        seek(offset + TIMELINE_MOVE_STEP)
    }

    fun speedUp() {
        tempo += SPEED_STEP
    }

    fun speedDown() {
        tempo -= SPEED_STEP
    }
}

@Composable
fun rememberHomeState(
    lorem: String = LOREM,
): HomeState {
    val stack = remember(lorem) {
        lorem
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }
    val state = remember(stack) { HomeState(stack = stack) }

    LaunchedEffect(key1 = state.isPlay) {
        if (state.offset >= state.count || !state.isPlay) return@LaunchedEffect

        while (state.offset < state.count - 1) {
            state.seek(state.offset + 1)
            delay(timeMillis = ((60.0 / state.tempo.coerceAtLeast(1)) * 1000).toLong().coerceAtLeast(1))
            if (!state.isPlay) return@LaunchedEffect
        }
    }

    LaunchedEffect(key1 = state.offset) {
        val index = state.offset.coerceIn(0, state.count - 1).toInt()
        state.currentWord = state.stack[index]
        state.progress = if (state.count > 0) (((index + 1).toLong() * 100) / state.count) else 0
    }

    return state
}

