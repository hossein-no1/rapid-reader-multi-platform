package com.util.rsvp

import androidx.compose.runtime.Composable

interface PdfTextPicker {
    fun launch()
}

@Composable
expect fun rememberPdfTextPicker(
    onResult: (String) -> Unit,
): PdfTextPicker?

@Composable
expect fun rememberPdfTextDropListener(
    onResult: (String) -> Unit,
): Boolean

