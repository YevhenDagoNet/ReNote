package com.example.renote.data

import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime

/**
 * The only way the app reaches the notes table. Owns the `createdAt` / `updatedAt` stamps so
 * no caller has to remember to set them.
 */
class NoteRepository(
    private val dao: NoteDao,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    /** A day's notes, already in the canonical order (spec §3). */
    fun observeByDate(date: LocalDate): Flow<List<Note>> = dao.observeByDate(date)

    suspend fun getById(id: Long): Note? = dao.getById(id)

    /** Inserts a new note on [date] and returns its id. */
    suspend fun create(
        date: LocalDate,
        title: String,
        description: String,
        time: LocalTime?,
    ): Long {
        val now = clock.instant()
        return dao.insert(
            Note(
                title = title,
                description = description,
                date = date,
                time = time,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    /** Saves [note] as given, refreshing only `updatedAt`. */
    suspend fun update(note: Note) {
        dao.update(note.copy(updatedAt = clock.instant()))
    }

    suspend fun delete(note: Note) {
        dao.delete(note)
    }
}
