package com.synq.app.ui.theme

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

private val FallbackDark = darkColorScheme(
    primary = Color(0xFFFFB68E),
    onPrimary = Color(0xFF4A2500),
    primaryContainer = Color(0xFF6D381E),
    onPrimaryContainer = Color(0xFFFFDBC8),
    secondary = Color(0xFFC08A5C),
    onSecondary = Color(0xFF3E2200),
    secondaryContainer = Color(0xFF5A3500),
    onSecondaryContainer = Color(0xFFFFDCBE),
    tertiary = Color(0xFFC3CB9C),
    onTertiary = Color(0xFF2A3200),
    tertiaryContainer = Color(0xFF414919),
    onTertiaryContainer = Color(0xFFE0E7BD),
    background = Color(0xFF1A1210),
    onBackground = Color(0xFFF0E5DD),
    surface = Color(0xFF1A1210),
    onSurface = Color(0xFFF0E5DD),
    surfaceVariant = Color(0xFF2D201B),
    onSurfaceVariant = Color(0xFFD8C3B9),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFFA08E84),
)

private val FallbackLight = lightColorScheme(
    primary = SynqPrimary,
    onPrimary = SynqOnPrimary,
    primaryContainer = SynqPrimaryContainer,
    onPrimaryContainer = SynqOnPrimaryContainer,
    secondary = Terracotta,
    onSecondary = SynqOnPrimary,
    secondaryContainer = SandLight,
    onSecondaryContainer = TextPrimary,
    tertiary = Olive,
    onTertiary = SynqOnPrimary,
    tertiaryContainer = OliveContainer,
    onTertiaryContainer = Color(0xFF191E05),
    background = SynqBackground,
    onBackground = TextPrimary,
    surface = SynqSurface,
    onSurface = TextPrimary,
    surfaceVariant = SynqSurfaceHigh,
    onSurfaceVariant = TextSecondary,
    error = Color(0xFFBA1A1A),
    onError = SynqOnPrimary,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = TextMuted,
)

@Composable
fun SynqTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FallbackDark
        else -> FallbackLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SynqTypography,
        content = content
    )
}
