package com.util.rsvp

internal expect suspend fun downloadTextFromUrl(
    url: String,
): String

