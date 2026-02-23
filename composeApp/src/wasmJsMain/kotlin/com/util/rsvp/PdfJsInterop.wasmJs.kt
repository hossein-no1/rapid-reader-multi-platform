@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.util.rsvp

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
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.Promise

private external interface PdfJsLib : JsAny {
    fun getDocument(src: JsAny): PdfLoadingTask
}

private external interface PdfLoadingTask : JsAny {
    val promise: Promise<JsAny?>
}

private external interface PdfDocumentProxy : JsAny {
    val numPages: Int
    fun getPage(pageNumber: Int): Promise<JsAny?>
}

private external interface PdfPageProxy : JsAny {
    fun getTextContent(): Promise<JsAny?>
}

private external interface PdfTextContent : JsAny {
    val items: JsArray<PdfTextItem>
}

private external interface PdfTextItem : JsAny {
    val str: String?
}

private fun pdfjsLib(): PdfJsLib? = js("globalThis.pdfjsLib")

private fun pdfDocParams(data: Uint8Array): JsAny = js("({ data: data })")

internal suspend fun extractTextFromPdf(file: File): String {
    val pdfjs = pdfjsLib() ?: return ""
    val buffer = withContext(Dispatchers.Default) { file.readAsArrayBuffer() }
    val data = Uint8Array(buffer)

    return extractTextFromPdfData(pdfjs, data)
}

internal suspend fun extractTextFromPdf(buffer: ArrayBuffer): String {
    val pdfjs = pdfjsLib() ?: return ""
    val data = Uint8Array(buffer)
    return extractTextFromPdfData(pdfjs, data)
}

private suspend fun extractTextFromPdfData(pdfjs: PdfJsLib, data: Uint8Array): String {
    val pdf = pdfjs
        .getDocument(pdfDocParams(data))
        .promise
        .await<JsAny?>()!!
        .unsafeCast<PdfDocumentProxy>()
    val numPages = pdf.numPages

    val sb = StringBuilder()
    for (i in 1..numPages) {
        val page = pdf.getPage(i).await<JsAny?>()!!.unsafeCast<PdfPageProxy>()
        val textContent = page.getTextContent().await<JsAny?>()!!.unsafeCast<PdfTextContent>()
        val items = textContent.items
        val length = items.length
        for (idx in 0 until length) {
            val s = items[idx]?.str
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
            val result = reader.result
            if (result == null) {
                cont.resumeWithException(IllegalStateException("Failed to read file"))
            } else {
                cont.resume(result.unsafeCast<ArrayBuffer>())
            }
        }
        reader.onerror = {
            cont.resumeWithException(IllegalStateException("Failed to read file"))
        }
        reader.readAsArrayBuffer(this)
    }

