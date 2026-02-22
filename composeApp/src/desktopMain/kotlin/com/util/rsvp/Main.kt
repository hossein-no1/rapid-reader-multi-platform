package com.util.rsvp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import rapidreader.composeapp.generated.resources.Res
import rapidreader.composeapp.generated.resources.rapid_reader_logo

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RapidReader",
        icon = painterResource(Res.drawable.rapid_reader_logo),
    ) {
        App()
    }
}

