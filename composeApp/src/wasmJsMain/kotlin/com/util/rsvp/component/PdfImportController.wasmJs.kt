@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.util.rsvp.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.util.rsvp.extractTextFromPdf
import com.util.rsvp.model.PdfHistoryItem
import com.util.rsvp.nowEpochMs
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File

@Composable
actual fun rememberPdfImportController(
    onPicked: (PdfHistoryItem) -> Unit,
    onResult: (String) -> Unit,
    onError: (String) -> Unit,
): PdfImportController? {
    val scope = rememberCoroutineScope()

    return remember(onPicked, onResult, onError) {
        object : PdfImportController {
            override fun launch() {
                val input = (document.createElement("input") as HTMLInputElement).apply {
                    type = "file"
                    accept = "application/pdf"
                }

                input.onchange = onchange@{ _: Event ->
                    val file = input.files?.item(0) ?: return@onchange
                    if (!file.name.endsWith(".pdf", ignoreCase = true)) {
                        onError("Please pick a .pdf file.")
                        return@onchange
                    }

                    scope.launch {
                        val (text, failure) = withContext(Dispatchers.Default) {
                            val result = runCatching { extractTextFromPdf(file.unsafeCast<File>()) }
                            result.getOrDefault("") to result.exceptionOrNull()
                        }
                        if (text.isBlank()) {
                            onError(failure?.message ?: "Couldn’t read this PDF.")
                        } else {
                            val item = PdfHistoryItem(
                                name = file.name,
                                uri = "local:${file.name}",
                                text = text,
                                addedAtEpochMs = nowEpochMs(),
                            )
                            onPicked(item)
                            onResult(text)
                        }
                    }
                }

                input.click()
            }
        }
    }
}

