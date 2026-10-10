package com.example.myapplication.ui.theme

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
    primary = HotelNavy,
    secondary = HotelCrvena,
    tertiary = Color(0xFF2E7D32),
)

private val LightColorScheme = lightColorScheme(
    primary = HotelNavy,
    onPrimary = HotelKrem,
    primaryContainer = HotelNavy,
    onPrimaryContainer = HotelKrem,

    secondary = HotelCrvena,
    onSecondary = Color.White,
    secondaryContainer = HotelCrvena,
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFF2E7D32),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF2E7D32),
    onTertiaryContainer = Color.White,

    background = HotelSiva,
    onBackground = HotelNavyTamna,

    surface = Color.White,
    onSurface = HotelNavyTamna,
    surfaceVariant = HotelKrem,
    onSurfaceVariant = HotelNavyTamna,

    error = HotelCrvena,
    onError = Color.White,
    errorContainer = Color(0xFFFDE8E7),
    onErrorContainer = HotelCrvena,

    outline = Color(0xFFB0B0B0),
    outlineVariant = Color(0xFFD8D8D8)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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