package com.example.renote.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface NoteDao {

    /**
     * A day's notes in the canonical order: timed notes by time ascending, then untimed
     * notes by creation time. `(time IS NULL)` is 0 for timed and 1 for untimed rows.
     */
    @Query(
        """
        SELECT * FROM notes
        WHERE date = :date
        ORDER BY (time IS NULL), time ASC, createdAt ASC
        """
    )
    fun observeByDate(date: LocalDate): Flow<List<Note>>

    /** Notes in an inclusive date range, by date, then each day in the same order as [observeByDate]. */
    @Query(
        """
        SELECT * FROM notes
        WHERE date BETWEEN :from AND :to
        ORDER BY date ASC, (time IS NULL), time ASC, createdAt ASC
        """
    )
    fun observeByDateRange(from: LocalDate, to: LocalDate): Flow<List<Note>>

    @Query("SELECT DISTINCT date FROM notes WHERE date BETWEEN :from AND :to")
    fun observeDatesWithNotes(from: LocalDate, to: LocalDate): Flow<List<LocalDate>>

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): Note?
}
