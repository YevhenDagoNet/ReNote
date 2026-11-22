package com.example.renote.ui.weekly

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.renote.appContainer
import com.example.renote.data.Note
import com.example.renote.data.NoteRepository
import com.example.renote.navigation.Routes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/** Saved-state key for the Monday of the week the pager currently shows (ISO string). */
private const val KEY_VISIBLE_WEEK = "visibleWeek"

/** The week always starts on Monday, regardless of locale (spec §4). */
fun LocalDate.startOfWeek(): LocalDate = with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

data class WeeklyUiState(
    /** Monday of the week the pager is centred on. Fixed for the lifetime of this screen. */
    val startWeek: LocalDate,
    /**
     * Every day of the visible week and both neighbouring weeks, each in the DAO's order.
     * A day with no notes maps to an empty list; a day missing from the map is still loading.
     */
    val notesByDate: Map<LocalDate, List<Note>>,
    val today: LocalDate,
)

class WeeklyViewModel(
    private val savedStateHandle: SavedStateHandle,
    repository: NoteRepository,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    // Pinned into saved state so a restored process keeps the same anchor.
    private val startWeek: LocalDate =
        (savedStateHandle.get<String>(Routes.ARG_ANCHOR_DATE)?.let(LocalDate::parse)
            ?: today.also { savedStateHandle[Routes.ARG_ANCHOR_DATE] = it.toString() })
            .startOfWeek()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<WeeklyUiState> = savedStateHandle
        .getStateFlow(KEY_VISIBLE_WEEK, startWeek.toString())
        .map(LocalDate::parse)
        .flatMapLatest { visibleMonday ->
            // Neighbouring weeks are loaded too so a swipe never lands on a page that is loading.
            val from = visibleMonday.minusWeeks(1)
            val to = visibleMonday.plusWeeks(2).minusDays(1)
            repository.observeByDateRange(from, to).map { notes ->
                // groupBy keeps the DAO's order within each day.
                val byDate = notes.groupBy(Note::date)
                generateSequence(from) { it.plusDays(1) }
                    .takeWhile { !it.isAfter(to) }
                    .associateWith { byDate[it].orEmpty() }
            }
        }
        .map { WeeklyUiState(startWeek = startWeek, notesByDate = it, today = today) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeeklyUiState(startWeek = startWeek, notesByDate = emptyMap(), today = today),
        )

    /** [monday] is the first day of the week the pager has moved to. */
    fun onVisibleWeekChange(monday: LocalDate) {
        savedStateHandle[KEY_VISIBLE_WEEK] = monday.toString()
    }

    /**
     * The month the switcher's M opens from here (spec §5): today's month when today is in the
     * shown week, otherwise the month of the week's Thursday, which holds most of its days.
     */
    fun viewedMonth(): YearMonth {
        val monday = LocalDate.parse(savedStateHandle[KEY_VISIBLE_WEEK] ?: startWeek.toString())
        val todayInWeek = !today.isBefore(monday) && !today.isAfter(monday.plusDays(6))
        return YearMonth.from(if (todayInWeek) today else monday.plusDays(3))
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WeeklyViewModel(createSavedStateHandle(), appContainer.noteRepository)
            }
        }
    }
}
