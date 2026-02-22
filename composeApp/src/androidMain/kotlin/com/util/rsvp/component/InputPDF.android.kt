package com.util.rsvp.component

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun InputPDF(
    modifier: Modifier,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    // pdfbox-android needs one-time init.
    remember(context) { PDFBoxResourceLoader.init(context) }

    val scope = rememberCoroutineScope()
    var pickedFileName by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult

            successMessage = null
            errorMessage = null

            val displayName = context.contentResolver.displayName(uri)
            val mime = context.contentResolver.getType(uri)

            val looksLikePdf = when {
                displayName?.endsWith(".pdf", ignoreCase = true) == true -> true
                mime?.equals("application/pdf", ignoreCase = true) == true -> true
                else -> false
            }

            if (!looksLikePdf) {
                pickedFileName = null
                errorMessage = "Please pick a .pdf file."
                return@rememberLauncherForActivityResult
            }

            pickedFileName = displayName ?: "Selected PDF"

            scope.launch {
                val text = runCatching {
                    withContext(Dispatchers.IO) { context.extractTextFromPdf(uri) }
                }.getOrNull().orEmpty()

                if (text.isBlank()) {
                    errorMessage = "Couldn’t read this PDF."
                    successMessage = null
                } else {
                    onResult(text)
                    successMessage = "File uploaded successfully."
                    errorMessage = null
                }
            }
        },
    )

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
                    launcher.launch(arrayOf("application/pdf"))
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

private fun Context.extractTextFromPdf(uri: Uri): String? {
    return contentResolver.openInputStream(uri)?.use { input ->
        PDDocument.load(input).use { doc ->
            PDFTextStripper().getText(doc)
        }
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