package com.realsalmon.studentfit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Violet = Color(0xFF5B3DF5)
val VioletDeep = Color(0xFF3A1FD1)
val Mint = Color(0xFF2ED3A5)
val Coral = Color(0xFFFF7A59)
val Sky = Color(0xFF3DA9FC)

private val Light = lightColorScheme(
    primary = Violet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E1FF),
    onPrimaryContainer = Color(0xFF1C0A78),
    secondary = Mint,
    onSecondary = Color(0xFF00382A),
    secondaryContainer = Color(0xFFCFF7E9),
    onSecondaryContainer = Color(0xFF00382A),
    tertiary = Coral,
    tertiaryContainer = Color(0xFFFFE3DA),
    onTertiaryContainer = Color(0xFF5C1600),
    background = Color(0xFFF7F6FB),
    onBackground = Color(0xFF17161C),
    surface = Color(0xFFF7F6FB),
    onSurface = Color(0xFF17161C),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF0EEF7),
    surfaceContainerHigh = Color(0xFFEAE7F3),
    surfaceContainerHighest = Color(0xFFE3E0EE),
    surfaceVariant = Color(0xFFE6E3F0),
    onSurfaceVariant = Color(0xFF5E5B6B),
    outline = Color(0xFFCAC6D6),
    outlineVariant = Color(0xFFE3E0EC),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFB4A5FF),
    onPrimary = Color(0xFF240C9A),
    primaryContainer = Color(0xFF3A1FD1),
    onPrimaryContainer = Color(0xFFE7E1FF),
    secondary = Color(0xFF63E6BE),
    onSecondary = Color(0xFF00382A),
    secondaryContainer = Color(0xFF00513D),
    onSecondaryContainer = Color(0xFFCFF7E9),
    tertiary = Color(0xFFFFA286),
    tertiaryContainer = Color(0xFF7A2A12),
    onTertiaryContainer = Color(0xFFFFE3DA),
    background = Color(0xFF0F0E14),
    onBackground = Color(0xFFE7E5EE),
    surface = Color(0xFF0F0E14),
    onSurface = Color(0xFFE7E5EE),
    surfaceContainerLowest = Color(0xFF0B0A0F),
    surfaceContainerLow = Color(0xFF17161E),
    surfaceContainer = Color(0xFF1C1B24),
    surfaceContainerHigh = Color(0xFF24232D),
    surfaceContainerHighest = Color(0xFF2E2D38),
    surfaceVariant = Color(0xFF2E2D38),
    onSurfaceVariant = Color(0xFFB0ADBD),
    outline = Color(0xFF4A4856),
    outlineVariant = Color(0xFF34333F),
)

private val AppTypography = Typography().let { t ->
    t.copy(
        displaySmall = t.displaySmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        headlineMedium = t.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        headlineSmall = t.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = t.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/** 숫자 강조용 스타일 */
val NumberStyle = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, letterSpacing = (-0.5).sp)

@Composable
fun StudentFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) Dark else Light,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
