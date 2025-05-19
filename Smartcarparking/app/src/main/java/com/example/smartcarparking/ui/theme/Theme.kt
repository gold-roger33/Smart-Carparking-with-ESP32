package com.example.smartcarparking.ui.theme

import android.app.Activity
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



private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFBB00),       // Gold/Yellow for primary
    secondary = Color(0xFF0E0E0E),     // Dark for secondary
    tertiary = Color(0xFFFFFEDA),      // Cream for tertiary
    background = Color(0xFF2C2C2C),    // Dark background
    surface = Color(0xFF2C2C2C),       // Surface color
    onPrimary = Color.Black,           // Text on primary
    onSecondary = Color(0xFFFFBB00),   // Gold text on secondary
    onTertiary = Color.Black,          // Black text on tertiary
    onBackground = Color(0xFFFFFEDA),  // Cream text on background
    onSurface = Color(0xFFFFFEDA),     // Cream text on surface
    primaryContainer = Color(0xFF0E0E0E)      // Container color
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFFBB00),
    secondary = Color(0xFF0E0E0E),
    tertiary = Color(0xFFFFFEDA),
    background = Color(0xFF2C2C2C),
    surface = Color(0xFF2C2C2C),
    onPrimary = Color.Black,
    onSecondary = Color(0xFFFFBB00),
    onTertiary = Color.Black,
    onBackground = Color(0xFFFFFEDA),
    onSurface = Color(0xFFFFFEDA),
    primaryContainer = Color(0xFF0E0E0E)
)

@Composable
fun SmartCarParkingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
        typography = AppTypography,
        content = content
    )
}

// Define your color constants here
val goldColor = Color(0xFFFFBB00)
val darkColor = Color(0xFF0E0E0E)
val creamColor = Color(0xFFFFFEDA)
val backgroundColor = Color(0xFF2C2C2C)