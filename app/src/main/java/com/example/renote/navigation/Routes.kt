package com.example.renote.navigation

import java.time.LocalDate

/** How the Daily view was reached; decides whether "<" is shown (spec §5). */
enum class DailyOrigin(val routeValue: String) {
    NONE("none"),
    WEEK("week"),
    MONTH("month");

    companion object {
        fun fromRouteValue(value: String?): DailyOrigin =
            entries.firstOrNull { it.routeValue == value } ?: NONE
    }
}

/** Route patterns and builders from spec §5. Dates travel as ISO `yyyy-MM-dd`. */
object Routes {
    const val ARG_DATE = "date"
    const val ARG_FROM = "from"
    const val ARG_NOTE_ID = "noteId"

    /** A missing [ARG_DATE] means today; only the start destination relies on that. */
    const val DAILY = "daily/{$ARG_DATE}?$ARG_FROM={$ARG_FROM}"
    const val DETAIL_NEW = "detail/new?$ARG_DATE={$ARG_DATE}"
    const val DETAIL_EDIT = "detail/{$ARG_NOTE_ID}"

    fun daily(date: LocalDate, from: DailyOrigin = DailyOrigin.NONE): String =
        "daily/$date?$ARG_FROM=${from.routeValue}"

    fun detailNew(date: LocalDate): String = "detail/new?$ARG_DATE=$date"

    fun detailEdit(noteId: Long): String = "detail/$noteId"
}
