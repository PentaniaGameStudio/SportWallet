package com.sportwallet.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// --- Assure-toi que ces couleurs existent dans Color.kt ---
// Light:
/// PinkPrimary, PinkPrimaryVariant, PinkSecondary,
/// BackgroundLight, SurfaceLight, OnPrimaryLight, OnSurfaceLight
// Dark:
/// PinkPrimaryDark, PinkSecondaryDark,
/// BackgroundDark, SurfaceDark, OnPrimaryDark, OnSurfaceDark

private val DarkColorScheme = darkColorScheme(
    primary = PinkPrimaryDark,
    secondary = PinkSecondaryDark,
    tertiary = PinkSecondaryDark,

    background = BackgroundDark,
    surface = SurfaceDark,

    onPrimary = OnPrimaryDark,
    onSecondary = Color(0xFF000000),
    onTertiary = Color(0xFF000000),

    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    secondary = PinkSecondary,
    tertiary = PinkPrimaryVariant,

    background = BackgroundLight,
    surface = SurfaceLight,

    onPrimary = OnPrimaryLight,
    onSecondary = OnPrimaryLight,
    onTertiary = OnPrimaryLight,

    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight
)

@Composable
fun SportWalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // IMPORTANT: si true, Android 12+ remplace ta palette par Material You
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
