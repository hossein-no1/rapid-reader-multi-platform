package com.util.rsvp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.darwin.NSObject

@Composable
actual fun rememberPdfTextPicker(
    onResult: (String) -> Unit,
): PdfTextPicker? {
    return remember(onResult) {
        object : PdfTextPicker {
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
                        val text = PDFDocument(uRL = url).string?.toString().orEmpty()
                        if (text.isNotBlank()) onResult(text)
                        controller.dismissViewControllerAnimated(true, completion = null)
                    }

                    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                        controller.dismissViewControllerAnimated(true, completion = null)
                    }
                }

                picker.delegate = delegate

                val root = topViewController() ?: return
                root.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

@Composable
actual fun rememberPdfTextDropListener(
    onResult: (String) -> Unit,
): Boolean = false

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

