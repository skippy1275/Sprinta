package za.co.sprinta.app.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val MontserratFontFamily = FontFamily.SansSerif
val InterFontFamily = FontFamily.Default

val SprintaTypography = Typography(
    // Hero Stat: 52sp Montserrat ExtraBold with Tabular Numerals
    displayLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 52.sp,
        lineHeight = 58.sp,
        fontFeatureSettings = "tnum"
    ),
    // H1: 28sp Bold Montserrat
    headlineLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    // H2: 22sp SemiBold Montserrat
    headlineMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    // Title: 18sp SemiBold Montserrat
    titleLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    // Title Medium / Stat Value: 20sp Bold
    titleMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontFeatureSettings = "tnum"
    ),
    // Body: 16sp Inter Regular
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    // Body Medium: 14sp Inter Regular
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Label: 14sp Inter Medium
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    // Label Small / Stat Label: 12sp Inter SemiBold
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // Caption: 12sp Inter Regular
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)
