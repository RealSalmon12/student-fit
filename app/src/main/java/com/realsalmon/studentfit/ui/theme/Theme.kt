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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.realsalmon.studentfit.R

/*
 * 디자인 기준: "정렬선 + 체육관"
 * - 분필색 바탕, 남색 글씨, 코트 라인 파랑(버튼·선택), 노랑은 정렬선에만 써요.
 * - 큰 숫자·제목은 등번호 같은 검은고딕, 본문은 프리텐다드.
 */
val Chalk = Color(0xFFF5F7F4)
val Ink = Color(0xFF1B2433)
val Court = Color(0xFF2B5CE6)
val Plumb = Color(0xFFFFC93C)
val Slate = Color(0xFF6B7587)
val Line = Color(0xFFDDE2E8)

// 예전 이름(다른 화면에서 쓰는 강조색)은 새 팔레트로 이어 줘요.
val Violet = Court
val VioletDeep = Ink
val Mint = Color(0xFF1F9D74)
val Coral = Color(0xFFE5484D)
val Sky = Color(0xFF5B8DEF)

val Display = FontFamily(Font(R.font.black_han_sans))
val Body = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

private val Light = lightColorScheme(
    primary = Court,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EAFD),
    onPrimaryContainer = Color(0xFF0E2A80),
    secondary = Ink,
    onSecondary = Chalk,
    secondaryContainer = Color(0xFFE8ECF1),
    onSecondaryContainer = Ink,
    tertiary = Color(0xFF8A6100),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFF0C7),
    onTertiaryContainer = Color(0xFF4A3400),
    error = Coral,
    background = Chalk,
    onBackground = Ink,
    surface = Chalk,
    onSurface = Ink,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFECEFEC),
    surfaceContainerHigh = Color(0xFFE5E9E6),
    surfaceContainerHighest = Color(0xFFDCE1DE),
    surfaceVariant = Color(0xFFE8ECF1),
    onSurfaceVariant = Slate,
    outline = Color(0xFFB9C1CC),
    outlineVariant = Line,
)

private val Dark = darkColorScheme(
    primary = Color(0xFF8EAEFF),
    onPrimary = Color(0xFF0B1E5C),
    primaryContainer = Color(0xFF1D3A99),
    onPrimaryContainer = Color(0xFFE3EAFD),
    secondary = Chalk,
    onSecondary = Ink,
    secondaryContainer = Color(0xFF2A3446),
    onSecondaryContainer = Chalk,
    tertiary = Plumb,
    onTertiary = Color(0xFF3B2900),
    tertiaryContainer = Color(0xFF4A3A10),
    onTertiaryContainer = Color(0xFFFFF0C7),
    error = Color(0xFFFF8589),
    background = Color(0xFF121826),
    onBackground = Color(0xFFE9ECEF),
    surface = Color(0xFF121826),
    onSurface = Color(0xFFE9ECEF),
    surfaceContainerLowest = Color(0xFF0D1220),
    surfaceContainerLow = Color(0xFF19202F),
    surfaceContainer = Color(0xFF1F2737),
    surfaceContainerHigh = Color(0xFF263042),
    surfaceContainerHighest = Color(0xFF2F3A4E),
    surfaceVariant = Color(0xFF2A3446),
    onSurfaceVariant = Color(0xFFA6B0BF),
    outline = Color(0xFF4A5568),
    outlineVariant = Color(0xFF2E3848),
)

private val AppTypography = Typography().let { t ->
    fun TextStyle.body() = copy(fontFamily = Body)
    fun TextStyle.display(size: Int, line: Int) = copy(
        fontFamily = Display, fontWeight = FontWeight.Normal, fontSize = size.sp, lineHeight = line.sp, letterSpacing = 0.sp,
    )
    t.copy(
        displayLarge = t.displayLarge.display(56, 60),
        displayMedium = t.displayMedium.display(44, 50),
        displaySmall = t.displaySmall.display(34, 40),
        headlineLarge = t.headlineLarge.display(30, 36),
        headlineMedium = t.headlineMedium.display(26, 32),
        headlineSmall = t.headlineSmall.copy(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp),
        titleLarge = t.titleLarge.copy(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = (-0.2).sp),
        titleMedium = t.titleMedium.copy(fontFamily = Body, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        titleSmall = t.titleSmall.copy(fontFamily = Body, fontWeight = FontWeight.SemiBold),
        bodyLarge = t.bodyLarge.copy(fontFamily = Body, lineHeight = 25.sp),
        bodyMedium = t.bodyMedium.copy(fontFamily = Body, lineHeight = 21.sp),
        bodySmall = t.bodySmall.body(),
        labelLarge = t.labelLarge.copy(fontFamily = Body, fontWeight = FontWeight.SemiBold),
        labelMedium = t.labelMedium.copy(fontFamily = Body, fontWeight = FontWeight.Medium),
        labelSmall = t.labelSmall.body(),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

/** 등번호 같은 큰 숫자 */
val NumberStyle = TextStyle(fontFamily = Display, fontSize = 30.sp, lineHeight = 34.sp)

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
