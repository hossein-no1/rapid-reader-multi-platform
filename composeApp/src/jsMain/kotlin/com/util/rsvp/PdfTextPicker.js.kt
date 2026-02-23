package com.util.rsvp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
                    val file = input.files?.item(0) ?: return@onchange null
                    scope.launch {
                        val text = extractTextFromPdf(file)
                        if (text.isNotBlank()) onResult(text)
                    }
                    null
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

private suspend fun extractTextFromPdf(file: File): String {
    val buffer = withContext(Dispatchers.Default) { file.readAsArrayBuffer() }
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

