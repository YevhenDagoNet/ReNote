package com.example.renote.ui.monthly

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.renote.appContainer
import com.example.renote.data.NoteRepository
import com.example.renote.navigation.Routes
import com.example.renote.ui.weekly.startOfWeek
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

/** Saved-state key for the month the pager currently shows (`yyyy-MM`). */
private const val KEY_VISIBLE_MONTH = "visibleMonth"

/** The grid always has 6 rows of 7 days, so its height never changes between months. */
const val MONTH_GRID_DAYS = 42

/** The Monday that opens the 6-week grid of this month (spec §6.3). */
fun YearMonth.gridStart(): LocalDate = atDay(1).startOfWeek()

/** The last day of the 6-week grid of this month, inclusive. */
fun YearMonth.gridEnd(): LocalDate = gridStart().plusDays(MONTH_GRID_DAYS - 1L)

data class MonthlyUiState(
    /** The month the pager is centred on. Fixed for the lifetime of this screen. */
    val startMonth: YearMonth,
    /**
     * Days with at least one note, covering the grids of the visible month and both
     * neighbours. Only the dates are loaded, never the notes themselves (spec §3).
     */
    val datesWithNotes: Set<LocalDate>,
    val today: LocalDate,
)

class MonthlyViewModel(
    private val savedStateHandle: SavedStateHandle,
    repository: NoteRepository,
) : ViewModel() {

    private val today: LocalDate = LocalDate.now()

    // Pinned into saved state so a restored process keeps the same anchor.
    private val startMonth: YearMonth =
        savedStateHandle.get<String>(Routes.ARG_YEAR_MONTH)?.let(YearMonth::parse)
            ?: YearMonth.from(today).also { savedStateHandle[Routes.ARG_YEAR_MONTH] = it.toString() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MonthlyUiState> = savedStateHandle
        .getStateFlow(KEY_VISIBLE_MONTH, startMonth.toString())
        .map(YearMonth::parse)
        .flatMapLatest { visible ->
            // Neighbouring months are loaded too so a swipe never lands on a page without dots.
            repository.observeDatesWithNotes(
                from = visible.minusMonths(1).gridStart(),
                to = visible.plusMonths(1).gridEnd(),
            )
        }
        .map { dates -> MonthlyUiState(startMonth = startMonth, datesWithNotes = dates.toSet(), today = today) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MonthlyUiState(startMonth = startMonth, datesWithNotes = emptySet(), today = today),
        )

    fun onVisibleMonthChange(month: YearMonth) {
        savedStateHandle[KEY_VISIBLE_MONTH] = month.toString()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MonthlyViewModel(createSavedStateHandle(), appContainer.noteRepository)
            }
        }
    }
}
