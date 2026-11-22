package com.example.renote.ui.monthly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.renote.R
import com.example.renote.ui.common.ViewMode
import com.example.renote.ui.common.ViewSwitcher
import com.example.renote.ui.common.rememberFullDateFormatter
import com.example.renote.ui.common.rememberMonthTitleFormatter
import com.example.renote.ui.daily.SampleNotes
import com.example.renote.ui.theme.ReNoteTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle

/** Pages are effectively unbounded in both directions; the start month sits in the middle. */
private const val PAGE_COUNT = Int.MAX_VALUE
private const val START_PAGE = PAGE_COUNT / 2

private const val GRID_ROWS = 6
private const val DAYS_PER_WEEK = 7
private const val OUTSIDE_MONTH_ALPHA = 0.38f

private val DayCircleSize = 36.dp
private val NoteDotSize = 6.dp
private val NoteDotGap = 4.dp

/** Monday-first, regardless of locale (spec §4). */
private val WeekDays: List<DayOfWeek> = List(DAYS_PER_WEEK) { DayOfWeek.MONDAY.plus(it.toLong()) }

private fun pageToMonth(startMonth: YearMonth, page: Int): YearMonth =
    startMonth.plusMonths((page - START_PAGE).toLong())

/** Screen-level entry point: the only place the Monthly ViewModel is collected. */
@Composable
fun MonthlyRoute(
    onDayClick: (date: LocalDate) -> Unit,
    onSwitchView: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MonthlyViewModel = viewModel(factory = MonthlyViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MonthlyScreen(
        startMonth = state.startMonth,
        datesWithNotes = state.datesWithNotes,
        today = state.today,
        onVisibleMonthChange = viewModel::onVisibleMonthChange,
        onDayClick = onDayClick,
        onSwitchView = onSwitchView,
        modifier = modifier,
    )
}

/**
 * Month view (spec §6.3). Stateless. No back button: the month is only reachable through the
 * switcher. [onVisibleMonthChange] reports the month the pager has moved to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyScreen(
    startMonth: YearMonth,
    datesWithNotes: Set<LocalDate>,
    today: LocalDate,
    onVisibleMonthChange: (YearMonth) -> Unit,
    onDayClick: (date: LocalDate) -> Unit,
    onSwitchView: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = START_PAGE, pageCount = { PAGE_COUNT })
    val visibleMonth = pageToMonth(startMonth, pagerState.currentPage)
    val titleFormatter = rememberMonthTitleFormatter()

    val currentOnVisibleMonthChange by rememberUpdatedState(onVisibleMonthChange)
    LaunchedEffect(pagerState, startMonth) {
        snapshotFlow { pagerState.currentPage }
            .collect { currentOnVisibleMonthChange(pageToMonth(startMonth, it)) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = visibleMonth.format(titleFormatter),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    ViewSwitcher(
                        selected = ViewMode.MONTH,
                        onSelect = onSwitchView,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
        ) {
            // The weekday header does not change between months, so it stays outside the pager.
            WeekdayHeader()
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                key = { it },
            ) { page ->
                MonthGrid(
                    month = pageToMonth(startMonth, page),
                    datesWithNotes = datesWithNotes,
                    today = today,
                    onDayClick = onDayClick,
                )
            }
        }
    }
}

@Composable
private fun WeekdayHeader(modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        WeekDays.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, locale),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Always 6 × 7 cells from the Monday on or before the 1st; neighbouring months are dimmed. */
@Composable
private fun MonthGrid(
    month: YearMonth,
    datesWithNotes: Set<LocalDate>,
    today: LocalDate,
    onDayClick: (date: LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val first = month.gridStart()
    Column(modifier = modifier.fillMaxSize()) {
        for (row in 0 until GRID_ROWS) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                for (column in 0 until DAYS_PER_WEEK) {
                    val date = first.plusDays((row * DAYS_PER_WEEK + column).toLong())
                    DayCell(
                        date = date,
                        inMonth = YearMonth.from(date) == month,
                        hasNotes = date in datesWithNotes,
                        isToday = date == today,
                        onClick = { onDayClick(date) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                    )
                }
            }
        }
    }
}

/** The day number, a filled circle behind it on today, and a dot below when it has notes. */
@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    hasNotes: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val alpha = if (inMonth) 1f else OUTSIDE_MONTH_ALPHA
    val fullDate = date.format(rememberFullDateFormatter())
    val description =
        if (hasNotes) stringResource(R.string.monthly_day_has_notes, fullDate) else fullDate
    val clickLabel = stringResource(R.string.monthly_open_day)

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                onClick(label = clickLabel) { onClick(); true }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(DayCircleSize)
                .then(if (isToday) Modifier.background(colors.primary, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = (if (isToday) colors.onPrimary else colors.onSurface).copy(alpha = alpha),
            )
        }
        Spacer(Modifier.height(NoteDotGap))
        // The dot's space is kept even without a dot, so numbers line up across a row.
        Box(
            modifier = Modifier
                .size(NoteDotSize)
                .then(
                    if (hasNotes) Modifier.background(colors.primary.copy(alpha = alpha), CircleShape)
                    else Modifier
                ),
        )
    }
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun MonthlyScreenPreview() {
    val month = YearMonth.of(2026, 11)
    ReNoteTheme {
        MonthlyScreen(
            startMonth = month,
            datesWithNotes = SampleNotes.between(month.gridStart(), month.gridEnd())
                .filterValues { it.isNotEmpty() }
                .keys,
            today = LocalDate.of(2026, 11, 22),
            onVisibleMonthChange = {},
            onDayClick = {},
            onSwitchView = {},
        )
    }
}
