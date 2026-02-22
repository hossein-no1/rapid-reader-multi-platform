package com.util.rsvp.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.util.rsvp.model.PdfHistoryItem
import com.util.rsvp.nowEpochMs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.awt.FileDialog
import java.io.File

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
                val dialog = FileDialog(
                    null as java.awt.Frame?,
                    "Select PDF",
                    FileDialog.LOAD,
                ).apply {
                    isMultipleMode = false
                    setFilenameFilter { _, name -> name.endsWith(".pdf", ignoreCase = true) }
                    isVisible = true
                }

                val dir = dialog.directory ?: return
                val fileName = dialog.file ?: return
                val picked = File(dir, fileName)

                if (!picked.extension.equals("pdf", ignoreCase = true)) {
                    onError("Please pick a .pdf file.")
                    return
                }

                scope.launch {
                    val text = withContext(Dispatchers.IO) { extractTextFromPdf(picked) }.orEmpty()
                    if (text.isBlank()) {
                        onError("Couldn’t read this PDF.")
                        return@launch
                    }

                    val item = PdfHistoryItem(
                        name = picked.name,
                        uri = picked.absolutePath,
                        text = text,
                        addedAtEpochMs = nowEpochMs(),
                    )
                    onPicked(item)
                    onResult(text)
                }
            }
        }
    }
}

private fun extractTextFromPdf(file: File): String? {
    if (!file.exists() || !file.isFile) return null
    if (!file.extension.equals("pdf", ignoreCase = true)) return null
    return PDDocument.load(file).use { doc ->
        PDFTextStripper().getText(doc)
    }
}

