package com.util.rsvp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.awt.FileDialog
import java.awt.KeyboardFocusManager
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.io.File

@Composable
actual fun rememberPdfTextPicker(
    onResult: (String) -> Unit,
): PdfTextPicker? {
    val scope = rememberCoroutineScope()
    return remember(onResult) {
        object : PdfTextPicker {
            override fun launch() {
                val dialog = FileDialog(null as java.awt.Frame?, "Select PDF", FileDialog.LOAD).apply {
                    isMultipleMode = false
                    setFilenameFilter { _, name -> name.endsWith(".pdf", ignoreCase = true) }
                    isVisible = true
                }
                val dir = dialog.directory ?: return
                val file = dialog.file ?: return
                val picked = File(dir, file)
                scope.launch {
                    val text = withContext(Dispatchers.IO) { extractTextFromPdf(picked) }
                    if (!text.isNullOrBlank()) onResult(text)
                }
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
        val dropTarget = DropTarget().apply {
            addDropTargetListener(object : DropTargetAdapter() {
                override fun drop(dtde: DropTargetDropEvent) {
                    try {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY)
                        val t = dtde.transferable
                        if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return
                        @Suppress("UNCHECKED_CAST")
                        val files = t.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
                        val pdf = files.firstOrNull { it.extension.equals("pdf", ignoreCase = true) } ?: return
                        scope.launch {
                            val text = withContext(Dispatchers.IO) { extractTextFromPdf(pdf) }
                            if (!text.isNullOrBlank()) onResult(text)
                        }
                    } finally {
                        dtde.dropComplete(true)
                    }
                }
            })
        }

        val attachJob = scope.launch {
            var lastWindow: java.awt.Window? = null
            while (true) {
                val active = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
                if (active != null && active !== lastWindow) {
                    lastWindow?.dropTarget = null
                    active.dropTarget = dropTarget
                    lastWindow = active
                }
                delay(250)
            }
        }

        onDispose {
            attachJob.cancel()
            val active = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
            if (active?.dropTarget === dropTarget) active.dropTarget = null
        }
    }

    return true
}

private fun extractTextFromPdf(file: File): String? {
    if (!file.exists() || !file.isFile) return null
    return PDDocument.load(file).use { doc ->
        PDFTextStripper().getText(doc)
    }
}

