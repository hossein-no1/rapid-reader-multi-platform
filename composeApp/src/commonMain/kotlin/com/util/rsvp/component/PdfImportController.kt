package com.util.rsvp.component

import androidx.compose.runtime.Composable
import com.util.rsvp.model.PdfHistoryItem

interface PdfImportController {
    fun launch()
}

@Composable
expect fun rememberPdfImportController(
    onPicked: (PdfHistoryItem) -> Unit,
    onResult: (String) -> Unit,
    onError: (String) -> Unit,
): PdfImportController?

