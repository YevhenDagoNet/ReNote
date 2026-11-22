package com.example.renote.ui.common

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.time.format.DateTimeFormatter

/**
 * Formatters built from the system's own patterns, so the locale's date order and the
 * device's 12/24h setting are respected (spec §4).
 */

/** Short date with weekday for screen titles, e.g. "Sun, Nov 22, 2026". */
@Composable
fun rememberDayTitleFormatter(): DateTimeFormatter {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale) {
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEMMMdyyyy"), locale)
    }
}

/** Hours and minutes in the device's 12/24h format. */
@Composable
fun rememberTimeFormatter(): DateTimeFormatter {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val is24Hour = DateFormat.is24HourFormat(context)
    return remember(locale, is24Hour) {
        val skeleton = if (is24Hour) "Hm" else "hm"
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
    }
}
