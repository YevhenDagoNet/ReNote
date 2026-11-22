package com.example.renote.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * A single note. [date] links it to a day and is fixed once the note is created.
 */
@Entity(
    tableName = "notes",
    indices = [Index("date")]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: LocalDate,
    val time: LocalTime? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        const val TITLE_MAX_LENGTH = 200
    }
}
