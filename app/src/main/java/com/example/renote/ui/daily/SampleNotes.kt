package com.example.renote.ui.daily

import com.example.renote.data.Note
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Hard-coded notes for Compose previews of the Daily and Weekly views. The running app reads from Room.
 *
 * Each list is written already in the DAO's order (timed by time, then untimed by createdAt),
 * because the UI must never re-sort.
 */
object SampleNotes {

    /** Notes for [date] and the days either side of it. */
    fun around(date: LocalDate): Map<LocalDate, List<Note>> =
        (-1L..1L).map(date::plusDays).associateWith(::forDate)

    /** Notes for every day from [from] to [to] inclusive. */
    fun between(from: LocalDate, to: LocalDate): Map<LocalDate, List<Note>> =
        generateSequence(from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(to) }
            .associateWith(::forDate)

    /** Cycles through four shapes of day so swiping shows each one. */
    private fun forDate(date: LocalDate): List<Note> = when (Math.floorMod(date.toEpochDay(), 4L).toInt()) {
        0 -> listOf(
            "Morning run" to LocalTime.of(7, 0),
            "Dentist appointment at the clinic on Main Street, bring the insurance card" to LocalTime.of(9, 30),
            "Team sync" to LocalTime.of(11, 0),
            "Lunch with Anna" to LocalTime.of(13, 15),
            "Buy groceries" to null,
            "Call the bank about the new card and ask why the last transfer took four days " +
                "to arrive, then update the budget spreadsheet accordingly" to null,
        )
        1 -> emptyList()
        2 -> (0 until 12).map { i ->
            "Task ${i + 1}" to if (i < 8) LocalTime.of(8 + i, 0) else null
        }
        else -> listOf("Read a chapter" to null)
    }.mapIndexed { index, (title, time) ->
        val created = Instant.ofEpochSecond(date.toEpochDay() * 86_400 + index)
        Note(
            id = date.toEpochDay() * 100 + index,
            title = title,
            date = date,
            time = time,
            createdAt = created,
            updatedAt = created,
        )
    }
}
