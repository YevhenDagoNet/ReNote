package com.example.renote.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.renote.R

/**
 * Caveat, a handwriting face from Google Fonts with Latin and Cyrillic glyphs, bundled as one
 * variable font (OFL, see licenses/Caveat-OFL.txt). Used for screen titles and day names only;
 * note text and input fields stay in the readable system sans (spec §7).
 */
@OptIn(ExperimentalTextApi::class) // Font(resId, variationSettings) for the variable weight axis.
private val Handwriting = FontFamily(
    Font(
        R.font.caveat,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.caveat,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
)

private val Default = Typography()

/**
 * Caveat sets small for its point size, so the title styles are larger than Material's
 * defaults. Body and label styles are Material's own.
 */
val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = Handwriting,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Handwriting,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Handwriting,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = Default.bodyLarge,
    bodyMedium = Default.bodyMedium,
    bodySmall = Default.bodySmall,
)
