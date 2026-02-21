package com.util.rsvp.theme

import androidx.compose.runtime.Composable
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import rsvp.composeapp.generated.resources.Res
import rsvp.composeapp.generated.resources.bold
import rsvp.composeapp.generated.resources.medium
import rsvp.composeapp.generated.resources.regular
import rsvp.composeapp.generated.resources.semi_bold

@OptIn(ExperimentalResourceApi::class)
@Composable
fun appTypography(): Typography {
    val appFont = FontFamily(
        Font(resource = Res.font.regular, weight = FontWeight.Normal),
        Font(resource = Res.font.medium, weight = FontWeight.Medium),
        Font(resource = Res.font.semi_bold, weight = FontWeight.SemiBold),
        Font(resource = Res.font.bold, weight = FontWeight.Bold),
    )
    val base = Typography()

    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = appFont),
        displayMedium = base.displayMedium.copy(fontFamily = appFont),
        displaySmall = base.displaySmall.copy(fontFamily = appFont),
        headlineLarge = base.headlineLarge.copy(fontFamily = appFont),
        headlineMedium = base.headlineMedium.copy(fontFamily = appFont),
        headlineSmall = base.headlineSmall.copy(fontFamily = appFont),
        titleLarge = base.titleLarge.copy(fontFamily = appFont, fontSize = 22.sp),
        titleMedium = base.titleMedium.copy(fontFamily = appFont),
        titleSmall = base.titleSmall.copy(fontFamily = appFont),
        bodyLarge = base.bodyLarge.copy(fontFamily = appFont, fontSize = 16.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = appFont),
        bodySmall = base.bodySmall.copy(fontFamily = appFont),
        labelLarge = base.labelLarge.copy(fontFamily = appFont),
        labelMedium = base.labelMedium.copy(fontFamily = appFont),
        labelSmall = base.labelSmall.copy(fontFamily = appFont),
    )
}