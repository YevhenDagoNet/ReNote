package com.example.renote.ui.daily

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.renote.R
import com.example.renote.data.Note
import com.example.renote.ui.common.NoteRow
import com.example.renote.ui.common.ViewMode
import com.example.renote.ui.common.ViewSwitcher
import com.example.renote.ui.common.rememberDayTitleFormatter
import com.example.renote.ui.theme.ReNoteTheme
import java.time.LocalDate

/** Pages are effectively unbounded in both directions; [startDate] sits in the middle. */
private const val PAGE_COUNT = Int.MAX_VALUE
private const val START_PAGE = PAGE_COUNT / 2

/** Space kept free below the last row so the FAB never covers it. */
private val FabClearance = 88.dp

private fun pageToDate(startDate: LocalDate, page: Int): LocalDate =
    startDate.plusDays((page - START_PAGE).toLong())

/** Screen-level entry point: the only place the Daily ViewModel is collected. */
@Composable
fun DailyRoute(
    onBack: () -> Unit,
    onAddNote: (date: LocalDate) -> Unit,
    onNoteClick: (noteId: Long) -> Unit,
    onSwitchView: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DailyViewModel = viewModel(factory = DailyViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DailyScreen(
        startDate = state.startDate,
        notesByDate = state.notesByDate,
        showBack = state.showBack,
        onVisibleDateChange = viewModel::onVisibleDateChange,
        onBack = onBack,
        onAddNote = onAddNote,
        onNoteClick = onNoteClick,
        onSwitchView = onSwitchView,
        modifier = modifier,
    )
}

/**
 * Daily view (spec §6.1). Stateless: [notesByDate] must contain the visible day and, ideally,
 * its neighbours so a swipe never shows an empty page while loading. Days missing from the
 * map render as empty. [onVisibleDateChange] reports the day the pager has moved to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    startDate: LocalDate,
    notesByDate: Map<LocalDate, List<Note>>,
    showBack: Boolean,
    onVisibleDateChange: (LocalDate) -> Unit,
    onBack: () -> Unit,
    onAddNote: (date: LocalDate) -> Unit,
    onNoteClick: (noteId: Long) -> Unit,
    onSwitchView: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = START_PAGE, pageCount = { PAGE_COUNT })
    val visibleDate = pageToDate(startDate, pagerState.currentPage)
    val titleFormatter = rememberDayTitleFormatter()

    val currentOnVisibleDateChange by rememberUpdatedState(onVisibleDateChange)
    LaunchedEffect(pagerState, startDate) {
        snapshotFlow { pagerState.currentPage }
            .collect { currentOnVisibleDateChange(pageToDate(startDate, it)) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = visibleDate.format(titleFormatter),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    }
                },
                actions = {
                    ViewSwitcher(
                        selected = ViewMode.DAY,
                        onSelect = onSwitchView,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddNote(visibleDate) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.action_add_note),
                )
            }
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            key = { it },
        ) { page ->
            DayNoteList(
                notes = notesByDate[pageToDate(startDate, page)].orEmpty(),
                onNoteClick = onNoteClick,
            )
        }
    }
}

@Composable
private fun DayNoteList(
    notes: List<Note>,
    onNoteClick: (noteId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(notes, key = { it.id }) { note ->
            NoteRow(
                title = note.title,
                time = note.time,
                onClick = { onNoteClick(note.id) },
            )
            HorizontalDivider()
        }
        item { Spacer(Modifier.height(FabClearance)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun DailyScreenPreview() {
    val today = LocalDate.of(2026, 11, 22)
    ReNoteTheme {
        DailyScreen(
            startDate = today,
            notesByDate = SampleNotes.around(today),
            showBack = true,
            onVisibleDateChange = {},
            onBack = {},
            onAddNote = {},
            onNoteClick = {},
            onSwitchView = {},
        )
    }
}
