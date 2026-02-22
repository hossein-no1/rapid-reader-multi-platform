package com.util.rsvp.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun InputPDF(
    modifier: Modifier = Modifier,
    onResult : (String) -> Unit
)