package com.util.rsvp.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun Content(modifier: Modifier = Modifier, word: String) {
    Text(
        modifier = modifier,
        text = word,
        fontSize = 48.sp,
        color = Color.White
    )
}