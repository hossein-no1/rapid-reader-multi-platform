package com.util.rsvp

import kotlinx.browser.window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal actual suspend fun downloadTextFromUrl(
    url: String,
): String {
    val response = window.fetch(url).await()
    if (!response.ok) return ""

    val contentType = response.headers.get("content-type").orEmpty()
    val looksLikePdf = contentType.contains("pdf", ignoreCase = true) ||
        url.substringBefore('?').endsWith(".pdf", ignoreCase = true)

    return if (looksLikePdf) {
        val blob = response.blob().await()
        val file = js("new File([blob], 'download.pdf', { type: blob.type || 'application/pdf' })")
            .unsafeCast<File>()
        extractTextFromPdfJs(file)
    } else {
        response.text().await()
    }
}

private suspend fun extractTextFromPdfJs(file: File): String {
    val buffer = withContext(Dispatchers.Default) { file.readAsArrayBuffer() }
    val pdfjsLib = js("window.pdfjsLib")
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

