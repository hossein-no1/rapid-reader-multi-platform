package com.util.rsvp

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.files.File

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
        extractTextFromPdf(file)
    } else {
        response.text().await()
    }
}

