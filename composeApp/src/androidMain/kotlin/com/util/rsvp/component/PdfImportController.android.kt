package com.util.rsvp.component

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.util.rsvp.model.PdfHistoryItem
import com.util.rsvp.nowEpochMs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberPdfImportController(
    onPicked: (PdfHistoryItem) -> Unit,
    onResult: (String) -> Unit,
    onError: (String) -> Unit,
): PdfImportController? {
    val context = LocalContext.current
    remember(context) { PDFBoxResourceLoader.init(context) }
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult

            val displayName = context.contentResolver.displayName(uri)
            val mime = context.contentResolver.getType(uri)
            val looksLikePdf = when {
                displayName?.endsWith(".pdf", ignoreCase = true) == true -> true
                mime?.equals("application/pdf", ignoreCase = true) == true -> true
                else -> false
            }

            if (!looksLikePdf) {
                onError("Please pick a .pdf file.")
                return@rememberLauncherForActivityResult
            }

            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }

            scope.launch {
                val text = runCatching {
                    withContext(Dispatchers.IO) { context.extractTextFromPdf(uri) }
                }.getOrNull().orEmpty()

                if (text.isBlank()) {
                    onError("Couldn’t read this PDF.")
                    return@launch
                }

                val item = PdfHistoryItem(
                    name = displayName ?: "Selected PDF",
                    uri = uri.toString(),
                    text = text,
                    addedAtEpochMs = nowEpochMs(),
                )
                onPicked(item)
                onResult(text)
            }
        },
    )

    return remember(launcher) {
        object : PdfImportController {
            override fun launch() {
                launcher.launch(arrayOf("application/pdf"))
            }
        }
    }
}

private fun Context.extractTextFromPdf(uri: Uri): String? =
    contentResolver.openInputStream(uri)?.use { input ->
        PDDocument.load(input).use { doc ->
            PDFTextStripper().getText(doc)
        }
    }

private fun android.content.ContentResolver.displayName(uri: Uri): String? {
    val cursor: Cursor? = runCatching {
        query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
    }.getOrNull()

    cursor?.use {
        if (!it.moveToFirst()) return null
        val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx < 0) return null
        return it.getString(idx)
    }
    return null
}

