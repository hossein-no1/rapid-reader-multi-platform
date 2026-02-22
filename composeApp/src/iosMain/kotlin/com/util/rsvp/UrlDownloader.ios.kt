package com.util.rsvp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.PDFKit.PDFDocument

internal actual suspend fun downloadTextFromUrl(
    url: String,
): String = withContext(Dispatchers.Default) {
    val nsUrl = NSURL(string = url) ?: return@withContext ""
    val data: NSData = NSData.dataWithContentsOfURL(nsUrl) ?: return@withContext ""

    val looksLikePdf = url.substringBefore('?').endsWith(".pdf", ignoreCase = true)
    if (!looksLikePdf) {
        return@withContext NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString().orEmpty()
    }

    val tempPath = NSTemporaryDirectory() + "rsvp_download.pdf"
    if (!data.writeToFile(path = tempPath, atomically = true)) return@withContext ""
    val fileUrl = NSURL.fileURLWithPath(path = tempPath)
    return@withContext PDFDocument(uRL = fileUrl)?.string?.toString().orEmpty()
}

