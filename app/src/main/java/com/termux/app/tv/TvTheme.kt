package com.termux.app.tv

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Shapes
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme

private val TvDarkColorScheme = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF00201D),
    primaryContainer = Color(0xFF004F48),
    onPrimaryContainer = Color(0xFFA7F3EB),
    secondary = Color(0xFFB1CCC7),
    onSecondary = Color(0xFF1C3531),
    secondaryContainer = Color(0xFF334B47),
    onSecondaryContainer = Color(0xFFCDE8E3),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF2B1700),
    background = Color(0xFF0E1113),
    onBackground = Color(0xFFE6ECEF),
    surface = Color(0xFF161B1E),
    onSurface = Color(0xFFE6ECEF),
    surfaceVariant = Color(0xFF242C30),
    onSurfaceVariant = Color(0xFFB5C2C8),
    border = Color(0xFF3C494E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val TvTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun TermuxTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TvDarkColorScheme,
        shapes = Shapes(),
        typography = TvTypography,
        content = content
    )
}
