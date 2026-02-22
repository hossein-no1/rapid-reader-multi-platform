package com.util.rsvp.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.util.rsvp.model.PdfHistoryItem
import com.util.rsvp.nowEpochMs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
actual fun rememberPdfImportController(
    onPicked: (PdfHistoryItem) -> Unit,
    onResult: (String) -> Unit,
    onError: (String) -> Unit,
): PdfImportController? {
    val scope = rememberCoroutineScope()

    return remember(onPicked, onResult, onError) {
        object : PdfImportController {
            private var delegateRef: UIDocumentPickerDelegateProtocol? = null

            override fun launch() {
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

                        val name = url.lastPathComponent ?: "Selected PDF"
                        val ext = url.pathExtension
                        if (ext?.equals("pdf", ignoreCase = true) != true) {
                            onError("Please pick a .pdf file.")
                            controller.dismissViewControllerAnimated(true, completion = null)
                            return
                        }

                        scope.launch {
                            val text = withContext(Dispatchers.Default) {
                                PDFDocument(uRL = url)?.string?.toString().orEmpty()
                            }
                            if (text.isBlank()) {
                                onError("Couldn’t read this PDF.")
                            } else {
                                val item = PdfHistoryItem(
                                    name = name,
                                    uri = url.absoluteString,
                                    text = text,
                                    addedAtEpochMs = nowEpochMs(),
                                )
                                onPicked(item)
                                onResult(text)
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

                val root = topViewController() ?: return
                root.presentViewController(picker, animated = true, completion = null)
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

