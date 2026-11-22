package com.example.renote.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Light only in v1 (spec §7). Dynamic colour is deliberately not used: the cream-and-amber
 * look must be the same on every device.
 */
private val ReNoteColorScheme = lightColorScheme(
    primary = AmberDeep,
    onPrimary = OnAmberDeep,
    primaryContainer = Amber,
    onPrimaryContainer = OnAmber,
    // SegmentedButton fills its selected segment with secondaryContainer.
    secondary = AmberDeep,
    onSecondary = OnAmberDeep,
    secondaryContainer = Amber,
    onSecondaryContainer = OnAmber,
    tertiary = AmberDeep,
    onTertiary = OnAmberDeep,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = CreamDeep,
    onSurfaceVariant = InkSoft,
    surfaceContainerLowest = Cream,
    surfaceContainerLow = CreamDim,
    surfaceContainer = CreamDim,
    surfaceContainerHigh = CreamDeep,
    surfaceContainerHighest = CreamDeeper,
    outline = PencilLine,
    outlineVariant = PencilLineFaint,
)

@Composable
fun ReNoteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ReNoteColorScheme,
        typography = Typography,
        content = content,
    )
}
