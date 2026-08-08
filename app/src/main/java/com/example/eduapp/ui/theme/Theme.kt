package com.example.eduapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = BlueDeep,
    primaryContainer = BlueSoft,
    onPrimaryContainer = BlueDeep,
    secondary = BrandPink,
    onSecondary = PinkDeep,
    secondaryContainer = PinkSoft,
    onSecondaryContainer = PinkDeep,
    background = BrandWhite,
    onBackground = BlueDeep,
    surface = BrandWhite,
    onSurface = BlueDeep,
    surfaceVariant = BlueTint,
    onSurfaceVariant = BlueDeep,
    error = AnswerWrong,
    onError = BrandWhite
)

private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = BlueDeep,
    primaryContainer = BlueDeep,
    onPrimaryContainer = BlueSoft,
    secondary = BrandPink,
    onSecondary = PinkDeep,
    secondaryContainer = PinkDeep,
    onSecondaryContainer = PinkSoft,
    background = NightSurface,
    onBackground = BrandWhite,
    surface = NightSurface,
    onSurface = BrandWhite,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = BlueSoft,
    error = AnswerWrong,
    onError = BrandWhite
)

@Composable
fun EduAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}