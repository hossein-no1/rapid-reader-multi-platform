package com.util.rsvp.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val BrandRed = Color(0xFFC71D25)

private val DarkBackground = Color(0xFF080808)
private val DarkOnBackground = Color(0xFFF2F2F2)
private val DarkSurface = Color(0xFF111111)
private val DarkOnSurface = Color(0xFFF2F2F2)
private val DarkSurfaceVariant = Color(0xFF1A1A1A)
private val DarkOnSurfaceVariant = Color(0xFFB8B8B8)
private val DarkOutline = Color(0xFF2A2A2A)

private val LightBackground = Color(0xFFFAFAFA)
private val LightOnBackground = Color(0xFF111111)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF111111)
private val LightSurfaceVariant = Color(0xFFF2F2F2)
private val LightOnSurfaceVariant = Color(0xFF555555)
private val LightOutline = Color(0xFFDEDEDE)

val LightColorScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
)

val DarkColorScheme = darkColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
)