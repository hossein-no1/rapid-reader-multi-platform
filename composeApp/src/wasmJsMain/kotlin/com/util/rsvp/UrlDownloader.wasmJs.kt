@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.util.rsvp

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import kotlin.js.JsAny
import kotlin.js.Promise

private external interface FetchResponse : JsAny {
    val ok: Boolean
    val status: Int
    val statusText: String
    val headers: FetchHeaders
    fun arrayBuffer(): Promise<JsAny?>
    fun text(): Promise<JsAny?>
}

private external interface FetchHeaders : JsAny {
    fun get(name: String): String?
}

internal actual suspend fun downloadTextFromUrl(
    url: String,
): String {
    val response = try {
        window.fetch(url).await<JsAny?>()!!.unsafeCast<FetchResponse>()
    } catch (t: Throwable) {
        throw IllegalStateException(
            t.message ?: "Failed to fetch. This is usually blocked by CORS on the target site.",
            t,
        )
    }
    if (!response.ok) {
        val status = response.status
        val statusText = response.statusText
        throw IllegalStateException("HTTP $status $statusText")
    }

    val contentType = response.headers.get("content-type") ?: ""
    val looksLikePdf = contentType.contains("pdf", ignoreCase = true) ||
        url.substringBefore('?').endsWith(".pdf", ignoreCase = true)

    if (looksLikePdf) {
        val buffer = try {
            response.arrayBuffer().await<JsAny?>()!!.unsafeCast<ArrayBuffer>()
        } catch (t: Throwable) {
            throw IllegalStateException(t.message ?: "Failed to download PDF bytes.", t)
        }

        val pdfText = try {
            extractTextFromPdf(buffer)
        } catch (t: Throwable) {
            throw IllegalStateException(t.message ?: "Failed to parse PDF.", t)
        }

        if (pdfText.isBlank()) {
            throw IllegalStateException("Downloaded PDF, but no extractable text was found.")
        }
        return pdfText
    }

    return try {
        response.text().await<JsAny?>()!!.toString()
    } catch (t: Throwable) {
        throw IllegalStateException(t.message ?: "Failed to read response text.", t)
    }
}

