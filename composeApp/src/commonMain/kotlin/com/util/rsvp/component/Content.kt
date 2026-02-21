package com.util.rsvp.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

@Composable
fun Content(modifier: Modifier = Modifier, text: String) {
    if (text.isEmpty()) return

    val centerIndex = text.length / 2
    val leftText = text.substring(0, centerIndex)
    val centerChar = text[centerIndex].toString()
    val rightText = text.substring(centerIndex + 1)

    val textStyle = TextStyle(fontSize = 32.sp)
    val textMeasurer = rememberTextMeasurer()

    val leftWidth = textMeasurer.measure(
        AnnotatedString(leftText),
        style = textStyle
    ).size.width.toFloat()

    val centerWidth = textMeasurer.measure(
        AnnotatedString(centerChar),
        style = textStyle
    ).size.width.toFloat()

    val rightWidth = textMeasurer.measure(
        AnnotatedString(rightText),
        style = textStyle
    ).size.width.toFloat()

    val totalWidth = leftWidth + centerWidth + rightWidth

    val offsetPx = (leftWidth + centerWidth / 2f) - (totalWidth / 2f)

    val density = LocalDensity.current
    val offsetDp = with(density) { offsetPx.toDp() }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.offset(x = -offsetDp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(leftText, style = textStyle, color = MaterialTheme.colorScheme.onBackground)

            Text(
                text = centerChar,
                style = textStyle,
                color = MaterialTheme.colorScheme.primary
            )

            Text(rightText, style = textStyle, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}