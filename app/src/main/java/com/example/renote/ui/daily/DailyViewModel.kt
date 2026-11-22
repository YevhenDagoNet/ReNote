package com.example.renote.ui.daily

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
import com.example.renote.navigation.DailyOrigin
import com.example.renote.navigation.Routes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

/** Saved-state key for the day the pager currently shows (ISO string). */
private const val KEY_VISIBLE_DATE = "visibleDate"

data class DailyUiState(
    /** The day the pager is centred on. Fixed for the lifetime of this screen. */
    val startDate: LocalDate,
    /** The visible day and its two neighbours, each in the DAO's order. */
    val notesByDate: Map<LocalDate, List<Note>>,
    val showBack: Boolean,
)

class DailyViewModel(
    private val savedStateHandle: SavedStateHandle,
    repository: NoteRepository,
) : ViewModel() {

    // Pinned into saved state so a restored process keeps the same anchor even after midnight.
    private val startDate: LocalDate =
        savedStateHandle.get<String>(Routes.ARG_DATE)?.let(LocalDate::parse)
            ?: LocalDate.now().also { savedStateHandle[Routes.ARG_DATE] = it.toString() }

    private val showBack =
        DailyOrigin.fromRouteValue(savedStateHandle[Routes.ARG_FROM]) != DailyOrigin.NONE

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DailyUiState> = savedStateHandle
        .getStateFlow(KEY_VISIBLE_DATE, startDate.toString())
        .map(LocalDate::parse)
        .flatMapLatest { visible ->
            // Neighbours are loaded too so a swipe never lands on a page that is still loading.
            val days = listOf(visible.minusDays(1), visible, visible.plusDays(1))
            combine(days.map(repository::observeByDate)) { lists -> days.zip(lists).toMap() }
        }
        .map { DailyUiState(startDate = startDate, notesByDate = it, showBack = showBack) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DailyUiState(startDate = startDate, notesByDate = emptyMap(), showBack = showBack),
        )

    fun onVisibleDateChange(date: LocalDate) {
        savedStateHandle[KEY_VISIBLE_DATE] = date.toString()
    }

    /** The month the switcher's M opens from here: the month of the day being shown (spec §5). */
    fun viewedMonth(): YearMonth =
        YearMonth.from(LocalDate.parse(savedStateHandle[KEY_VISIBLE_DATE] ?: startDate.toString()))

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DailyViewModel(createSavedStateHandle(), appContainer.noteRepository)
            }
        }
    }
}
