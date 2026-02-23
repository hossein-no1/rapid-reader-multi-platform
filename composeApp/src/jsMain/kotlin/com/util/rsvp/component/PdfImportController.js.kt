package com.util.rsvp.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.util.rsvp.model.PdfHistoryItem
import com.util.rsvp.nowEpochMs
import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
                    val file = input.files?.item(0) ?: return@onchange null

                    if (!file.name.endsWith(".pdf", ignoreCase = true)) {
                        onError("Please pick a .pdf file.")
                        return@onchange null
                    }

                    scope.launch {
                        val (text, failure) = withContext(Dispatchers.Default) {
                            val result = runCatching { extractTextFromPdf(file) }
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
                    null
                }

                input.click()
            }
        }
    }
}

private suspend fun extractTextFromPdf(file: File): String {
    val buffer = file.readAsArrayBuffer()
    val pdfjsLib = js("globalThis.pdfjsLib")
    if (pdfjsLib == null) return ""

    val data = Uint8Array(buffer)
    val loadingTask = pdfjsLib.getDocument(js("{ data: data }"))
    val pdf = loadingTask.promise.await()
    val numPages = (pdf.numPages as Int)

    val sb = StringBuilder()
    for (i in 1..numPages) {
        val page = pdf.getPage(i).await()
        val textContent = page.getTextContent().await()
        val items = textContent.items.unsafeCast<Array<dynamic>>()
        for (item in items) {
            val s = item.str as? String
            if (!s.isNullOrBlank()) sb.append(s).append(' ')
        }
        sb.append('\n')
    }
    return sb.toString()
}

private suspend fun File.readAsArrayBuffer(): ArrayBuffer =
    suspendCancellableCoroutine { cont ->
        val reader = FileReader()
        reader.onload = {
            cont.resume(reader.result.unsafeCast<ArrayBuffer>())
            null
        }
        reader.onerror = {
            cont.resumeWithException(IllegalStateException("Failed to read file"))
            null
        }
        reader.readAsArrayBuffer(this)
    }

