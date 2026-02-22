package com.util.rsvp.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DriveFolderUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
actual fun InputPDF(
    modifier: Modifier,
    onResult: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var pickedFileName by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(.5F)
                .clickable {
                    successMessage = null
                    errorMessage = null

                    val input = (document.createElement("input") as HTMLInputElement).apply {
                        type = "file"
                        accept = "application/pdf"
                    }

                    input.onchange = onchange@{ _: Event ->
                        val file = input.files?.item(0) ?: return@onchange null

                        if (!file.name.endsWith(".pdf", ignoreCase = true)) {
                            pickedFileName = null
                            successMessage = null
                            errorMessage = "Please pick a .pdf file."
                            return@onchange null
                        }

                        pickedFileName = file.name
                        scope.launch {
                            val text = withContext(Dispatchers.Default) {
                                runCatching { extractTextFromPdf(file) }.getOrDefault("")
                            }

                            if (text.isBlank()) {
                                errorMessage = "Couldn’t read this PDF."
                                successMessage = null
                            } else {
                                onResult(text)
                                successMessage = "File uploaded successfully."
                                errorMessage = null
                            }
                        }
                        null
                    }

                    input.click()
                }
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val cornerRadius = 8.dp.toPx()

                    drawRoundRect(
                        color = Color.Gray,
                        size = size,
                        cornerRadius = CornerRadius(cornerRadius),
                        style = Stroke(
                            width = strokeWidth,
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(10f, 6f)
                            )
                        )
                    )
                }
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier.size(size = 64.dp),
                imageVector = Icons.Rounded.DriveFolderUpload,
                tint = Color.Gray.copy(alpha = .7F),
                contentDescription = "",
            )

            Text(
                text = pickedFileName ?: "Pick a PDF file",
                color = if (pickedFileName == null) Color.Gray else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (successMessage != null) {
                Text(
                    text = successMessage.orEmpty(),
                    color = Color(0xFF2E7D32),
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = Color(0xFFB00020),
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private suspend fun extractTextFromPdf(file: File): String {
    val buffer = file.readAsArrayBuffer()
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

