package com.example.renote.ui.common

import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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

/**
 * Formats an inclusive date range in the system's own range style, e.g. "Nov 16 – 22, 2026",
 * collapsing the shared month/year the way the locale does.
 */
@Composable
fun rememberDateRangeFormatter(): (start: LocalDate, end: LocalDate) -> String {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    return remember(context, locale) {
        { start, end ->
            // Noon on both ends: a midnight end would be treated as exclusive and drop a day.
            val zone = ZoneId.systemDefault()
            DateUtils.formatDateRange(
                context,
                start.atTime(LocalTime.NOON).atZone(zone).toInstant().toEpochMilli(),
                end.atTime(LocalTime.NOON).atZone(zone).toInstant().toEpochMilli(),
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_ABBREV_MONTH,
            )
        }
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
