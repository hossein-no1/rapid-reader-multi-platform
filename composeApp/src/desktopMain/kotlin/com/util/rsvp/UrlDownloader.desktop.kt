package com.util.rsvp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

internal actual suspend fun downloadTextFromUrl(
    url: String,
): String = withContext(Dispatchers.IO) {
    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
        instanceFollowRedirects = true
        connectTimeout = 15_000
        readTimeout = 30_000
    }

    try {
        val bytes = conn.inputStream.use { it.readBytes() }
        val contentType = conn.contentType.orEmpty()
        val looksLikePdf = contentType.contains("pdf", ignoreCase = true) ||
            url.substringBefore('?').endsWith(".pdf", ignoreCase = true) ||
            (bytes.size >= 4 && bytes[0] == '%'.code.toByte() && bytes[1] == 'P'.code.toByte() && bytes[2] == 'D'.code.toByte() && bytes[3] == 'F'.code.toByte())

        if (!looksLikePdf) return@withContext bytes.toString(Charsets.UTF_8)

        return@withContext runCatching {
            ByteArrayInputStream(bytes).use { input ->
                PDDocument.load(input).use { doc ->
                    PDFTextStripper().getText(doc)
                }
            }
        }.getOrNull().orEmpty()
    } finally {
        conn.disconnect()
    }
}

