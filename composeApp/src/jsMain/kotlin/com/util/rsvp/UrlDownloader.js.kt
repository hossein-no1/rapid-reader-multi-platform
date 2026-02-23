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
    val response = runCatching { window.fetch(url).await() }
        .getOrElse { t ->
            // Most common on web: CORS blocks cross-origin downloads ("TypeError: Failed to fetch").
            runCatching { fetchViaProxy(url) }.getOrElse { proxyFailure ->
                throw IllegalStateException(
                    t.message
                        ?: proxyFailure.message
                        ?: "Failed to fetch. This is usually blocked by CORS on the target site.",
                    t,
                )
            }
        }
    if (!response.ok) {
        val status = response.status
        val statusText = response.statusText
        throw IllegalStateException("HTTP $status $statusText")
    }

    val contentType = response.headers.get("content-type").orEmpty()
    val looksLikePdf = contentType.contains("pdf", ignoreCase = true) ||
        url.substringBefore('?').endsWith(".pdf", ignoreCase = true)

    return if (looksLikePdf) {
        val pdfText = runCatching {
            val blob = response.blob().await()
            val file = js("new File([blob], 'download.pdf', { type: blob.type || 'application/pdf' })")
                .unsafeCast<File>()
            extractTextFromPdfJs(file)
        }.getOrElse { t ->
            throw IllegalStateException(t.message ?: "Failed to parse PDF.", t)
        }

        if (pdfText.isBlank()) {
            throw IllegalStateException("Downloaded PDF, but no extractable text was found.")
        }
        pdfText
    } else {
        response.text().await()
    }
}

private suspend fun fetchViaProxy(url: String) =
    window.fetch("/api/proxy?url=" + js("encodeURIComponent(url)").unsafeCast<String>()).await()

private suspend fun extractTextFromPdfJs(file: File): String {
    val buffer = withContext(Dispatchers.Default) { file.readAsArrayBuffer() }
    val pdfjsLib = js("window.pdfjsLib")
    if (pdfjsLib == null) {
        throw IllegalStateException("PDF engine not loaded. Please refresh and try again.")
    }

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

