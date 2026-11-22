package com.example.renote.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var db: NoteDatabase
    private lateinit var dao: NoteDao

    private val day = LocalDate.of(2026, 3, 20)
    private val base = Instant.parse("2026-03-20T08:00:00Z")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java).build()
        dao = db.noteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun note(title: String, time: LocalTime?, createdOffsetSec: Long, date: LocalDate = day) =
        base.plusSeconds(createdOffsetSec).let { created ->
            Note(title = title, date = date, time = time, createdAt = created, updatedAt = created)
        }

    @Test
    fun observeByDate_timedByTimeFirst_thenUntimedByCreatedAt() = runTest {
        // Inserted deliberately out of order; createdAt order differs from time order.
        dao.insert(note("untimed-late", time = null, createdOffsetSec = 300))
        dao.insert(note("timed-14:00", time = LocalTime.of(14, 0), createdOffsetSec = 0))
        dao.insert(note("untimed-early", time = null, createdOffsetSec = 100))
        dao.insert(note("timed-09:30", time = LocalTime.of(9, 30), createdOffsetSec = 400))
        dao.insert(note("timed-00:00", time = LocalTime.MIDNIGHT, createdOffsetSec = 500))
        dao.insert(note("other-day", time = LocalTime.of(8, 0), createdOffsetSec = 0, date = day.plusDays(1)))

        val titles = dao.observeByDate(day).first().map { it.title }

        assertEquals(
            listOf("timed-00:00", "timed-09:30", "timed-14:00", "untimed-early", "untimed-late"),
            titles
        )
    }

    @Test
    fun observeByDateRange_byDate_thenCanonicalDayOrder_inclusiveBounds() = runTest {
        val next = day.plusDays(1)
        val last = day.plusDays(2)
        dao.insert(note("before-range", time = LocalTime.of(8, 0), createdOffsetSec = 0, date = day.minusDays(1)))
        dao.insert(note("last-untimed", time = null, createdOffsetSec = 0, date = last))
        dao.insert(note("next-untimed", time = null, createdOffsetSec = 0, date = next))
        dao.insert(note("day-untimed-late", time = null, createdOffsetSec = 300))
        dao.insert(note("next-timed-10:00", time = LocalTime.of(10, 0), createdOffsetSec = 500, date = next))
        dao.insert(note("day-timed-14:00", time = LocalTime.of(14, 0), createdOffsetSec = 0))
        dao.insert(note("day-untimed-early", time = null, createdOffsetSec = 100))
        dao.insert(note("day-timed-09:30", time = LocalTime.of(9, 30), createdOffsetSec = 400))
        dao.insert(note("after-range", time = null, createdOffsetSec = 0, date = day.plusDays(3)))

        val titles = dao.observeByDateRange(day, last).first().map { it.title }

        assertEquals(
            listOf(
                "day-timed-09:30", "day-timed-14:00", "day-untimed-early", "day-untimed-late",
                "next-timed-10:00", "next-untimed",
                "last-untimed",
            ),
            titles
        )
    }

    @Test
    fun observeDatesWithNotes_distinctDays_inclusiveBounds() = runTest {
        val last = day.plusDays(10)
        dao.insert(note("before-range", time = null, createdOffsetSec = 0, date = day.minusDays(1)))
        dao.insert(note("first-a", time = null, createdOffsetSec = 0))
        dao.insert(note("first-b", time = LocalTime.of(9, 0), createdOffsetSec = 100))
        dao.insert(note("middle", time = null, createdOffsetSec = 0, date = day.plusDays(4)))
        dao.insert(note("last", time = null, createdOffsetSec = 0, date = last))
        dao.insert(note("after-range", time = null, createdOffsetSec = 0, date = last.plusDays(1)))

        val dates = dao.observeDatesWithNotes(day, last).first()

        assertEquals(listOf(day, day.plusDays(4), last), dates.sorted())
    }
}
