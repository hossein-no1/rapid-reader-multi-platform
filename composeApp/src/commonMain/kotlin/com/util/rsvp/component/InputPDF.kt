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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.util.rsvp.model.PdfHistoryItem
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InputPDF(
    modifier: Modifier = Modifier,
    onPicked: (PdfHistoryItem) -> Unit,
    onResult : (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var pickedName by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    val controller = rememberPdfImportController(
        onPicked = {
            pickedName = it.name
            onPicked(it)
        },
        onResult = { text ->
            busy = false
            successMessage = "File uploaded successfully."
            errorMessage = null
            onResult(text)
        },
        onError = { msg ->
            busy = false
            successMessage = null
            errorMessage = msg
        },
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = controller != null && !busy) {
                    successMessage = null
                    errorMessage = null
                    busy = true
                    scope.launch { controller?.launch() }
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
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)),
                        ),
                    )
                }
                .padding(vertical = 20.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                modifier = Modifier.size(56.dp),
                imageVector = Icons.Rounded.DriveFolderUpload,
                tint = Color.Gray.copy(alpha = 0.7f),
                contentDescription = null,
            )

            Text(
                text = pickedName ?: "Pick a PDF file",
                color = if (pickedName == null) Color.Gray else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )

            Button(
                onClick = {
                    successMessage = null
                    errorMessage = null
                    busy = true
                    scope.launch { controller?.launch() }
                },
                enabled = controller != null && !busy,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background,
                    disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    disabledContentColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                ),
            ) {
                Text(if (busy) "Opening…" else "Browse")
            }

            when {
                successMessage != null -> Text(
                    text = successMessage.orEmpty(),
                    color = Color(0xFF2E7D32),
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodyMedium,
                )

                errorMessage != null -> Text(
                    text = errorMessage.orEmpty(),
                    color = Color(0xFFB00020),
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}