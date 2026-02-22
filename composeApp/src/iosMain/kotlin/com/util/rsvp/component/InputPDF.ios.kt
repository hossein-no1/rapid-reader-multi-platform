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
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@Composable
actual fun InputPDF(
    modifier: Modifier,
    onResult: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var pickedFileName by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var delegateRef by remember { mutableStateOf<UIDocumentPickerDelegateProtocol?>(null) }

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

                    val picker = UIDocumentPickerViewController(
                        documentTypes = listOf("com.adobe.pdf"),
                        inMode = UIDocumentPickerMode.UIDocumentPickerModeImport,
                    )

                    val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
                        override fun documentPicker(
                            controller: UIDocumentPickerViewController,
                            didPickDocumentsAtURLs: List<*>,
                        ) {
                            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return

                            val name = url.lastPathComponent
                            val ext = url.pathExtension
                            if (ext?.equals("pdf", ignoreCase = true) != true) {
                                pickedFileName = null
                                successMessage = null
                                errorMessage = "Please pick a .pdf file."
                                controller.dismissViewControllerAnimated(true, completion = null)
                                return
                            }

                            pickedFileName = name
                            scope.launch {
                                val text = withContext(Dispatchers.Default) {
                                    PDFDocument(uRL = url).string.orEmpty()
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

                            controller.dismissViewControllerAnimated(true, completion = null)
                        }

                        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                            controller.dismissViewControllerAnimated(true, completion = null)
                        }
                    }

                    delegateRef = delegate
                    picker.delegate = delegate

                    val root = topViewController() ?: return@clickable
                    root.presentViewController(picker, animated = true, completion = null)
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

private fun topViewController(): UIViewController? {
    val app = UIApplication.sharedApplication
    val root: UIViewController = app.keyWindow?.rootViewController ?: return null
    var top: UIViewController? = root
    while (true) {
        val presented = top?.presentedViewController ?: break
        top = presented
    }
    return top
}