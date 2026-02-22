package com.util.rsvp

sealed interface AppRoute {
    data object Gate : AppRoute
    data class Home(val text: String) : AppRoute
}

