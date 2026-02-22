@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.util.rsvp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event

@Composable
actual fun rememberPdfTextPicker(
    onResult: (String) -> Unit,
): PdfTextPicker? {
    val scope = rememberCoroutineScope()
    return remember(onResult) {
        object : PdfTextPicker {
            override fun launch() {
                val input = (document.createElement("input") as HTMLInputElement).apply {
                    type = "file"
                    accept = "application/pdf"
                }
                input.onchange = onchange@{ _: Event ->
                    val file = input.files?.item(0) ?: return@onchange
                    scope.launch {
                        val text = extractTextFromPdf(file)
                        if (text.isNotBlank()) onResult(text)
                    }
                }
                input.click()
            }
        }
    }
}

@Composable
actual fun rememberPdfTextDropListener(
    onResult: (String) -> Unit,
): Boolean {
    val scope = rememberCoroutineScope()

    DisposableEffect(onResult) {
        val prevent: (Event) -> Unit = { e ->
            e.preventDefault()
            e.stopPropagation()
        }
        val onDrop: (Event) -> Unit = { e ->
            val de = e.unsafeCast<DragEvent>()
            prevent(e)
            val file = de.dataTransfer?.files?.item(0)
            if (file != null) {
                scope.launch {
                    val text = extractTextFromPdf(file)
                    if (text.isNotBlank()) onResult(text)
                }
            }
        }

        window.addEventListener("dragover", prevent)
        window.addEventListener("drop", onDrop)

        onDispose {
            window.removeEventListener("dragover", prevent)
            window.removeEventListener("drop", onDrop)
        }
    }

    return true
}

