package com.example.renote.ui.weekly

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.renote.R
import com.example.renote.data.Note
import com.example.renote.ui.common.ViewMode
import com.example.renote.ui.common.ViewSwitcher
import com.example.renote.ui.common.rememberDateRangeFormatter
import com.example.renote.ui.daily.SampleNotes
import com.example.renote.ui.theme.ReNoteTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle

/** Pages are effectively unbounded in both directions; the start week sits in the middle. */
private const val PAGE_COUNT = Int.MAX_VALUE
private const val START_PAGE = PAGE_COUNT / 2

private val GridSpacing = 8.dp
private val TileShape = RoundedCornerShape(12.dp)
private val TileBorder = 1.dp
private val TodayTileBorder = 3.dp
private val ScrollThumbWidth = 3.dp

private fun pageToMonday(startWeek: LocalDate, page: Int): LocalDate =
    startWeek.plusWeeks((page - START_PAGE).toLong())

/** Screen-level entry point: the only place the Weekly ViewModel is collected. */
@Composable
fun WeeklyRoute(
    onDayClick: (date: LocalDate) -> Unit,
    onSwitchView: (mode: ViewMode, viewedMonth: YearMonth) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeeklyViewModel = viewModel(factory = WeeklyViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WeeklyScreen(
        startWeek = state.startWeek,
        notesByDate = state.notesByDate,
        today = state.today,
        onVisibleWeekChange = viewModel::onVisibleWeekChange,
        onDayClick = onDayClick,
        onSwitchView = { mode -> onSwitchView(mode, viewModel.viewedMonth()) },
        modifier = modifier,
    )
}

/**
 * Weekly view (spec §6.2). Stateless. [startWeek] is a Monday. A day missing from
 * [notesByDate] is treated as still loading and its tile renders blank, like an empty day.
 * [onVisibleWeekChange] reports the Monday of the week the pager has moved to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyScreen(
    startWeek: LocalDate,
    notesByDate: Map<LocalDate, List<Note>>,
    today: LocalDate,
    onVisibleWeekChange: (monday: LocalDate) -> Unit,
    onDayClick: (date: LocalDate) -> Unit,
    onSwitchView: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = START_PAGE, pageCount = { PAGE_COUNT })
    val visibleMonday = pageToMonday(startWeek, pagerState.currentPage)
    val formatRange = rememberDateRangeFormatter()

    val currentOnVisibleWeekChange by rememberUpdatedState(onVisibleWeekChange)
    LaunchedEffect(pagerState, startWeek) {
        snapshotFlow { pagerState.currentPage }
            .collect { currentOnVisibleWeekChange(pageToMonday(startWeek, it)) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = formatRange(visibleMonday, visibleMonday.plusDays(6)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    ViewSwitcher(
                        selected = ViewMode.WEEK,
                        onSelect = onSwitchView,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                },
            )
        },
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            key = { it },
        ) { page ->
            WeekGrid(
                monday = pageToMonday(startWeek, page),
                notesByDate = notesByDate,
                today = today,
                onDayClick = onDayClick,
            )
        }
    }
}

/**
 * Fills the page exactly, with no outer scroll: Mon–Sat in a 2 × 3 grid, then Sunday at full
 * width. A non-scrolling outer surface lets every tile scroll on its own without conflict.
 */
@Composable
private fun WeekGrid(
    monday: LocalDate,
    notesByDate: Map<LocalDate, List<Note>>,
    today: LocalDate,
    onDayClick: (date: LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    @Composable
    fun Tile(dayOffset: Long, tileModifier: Modifier) {
        val date = monday.plusDays(dayOffset)
        DayTile(
            date = date,
            notes = notesByDate[date].orEmpty(),
            isToday = date == today,
            onClick = { onDayClick(date) },
            modifier = tileModifier,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(GridSpacing),
        verticalArrangement = Arrangement.spacedBy(GridSpacing),
    ) {
        for (row in 0L until 3L) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(GridSpacing),
            ) {
                Tile(dayOffset = row * 2, tileModifier = Modifier.weight(1f).fillMaxSize())
                Tile(dayOffset = row * 2 + 1, tileModifier = Modifier.weight(1f).fillMaxSize())
            }
        }
        Tile(dayOffset = 6, tileModifier = Modifier.fillMaxWidth().weight(1f))
    }
}

/** One day of the week: its name, then its notes as one-line titles in the DAO's order. */
@Composable
private fun DayTile(
    date: LocalDate,
    notes: List<Note>,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val listState = rememberLazyListState()
    val borderColor =
        if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val thumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Column(
        modifier = modifier
            .clip(TileShape)
            .border(if (isToday) TodayTileBorder else TileBorder, borderColor, TileShape)
            .clickable(onClickLabel = stringResource(R.string.weekly_open_day), onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
                .verticalScrollIndicator(listState, thumbColor),
        ) {
            items(notes, key = { it.id }) { note ->
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        // End padding keeps the scroll thumb clear of the text.
                        .padding(top = 2.dp, bottom = 2.dp, end = ScrollThumbWidth * 2),
                )
            }
        }
    }
}

/**
 * A thin thumb along the right edge, drawn only while the list has more content than fits.
 * Its size and position are estimated from item counts, which is accurate enough for
 * uniformly sized one-line rows.
 */
private fun Modifier.verticalScrollIndicator(state: LazyListState, color: Color): Modifier =
    drawWithContent {
        drawContent()
        if (!state.canScrollForward && !state.canScrollBackward) return@drawWithContent
        val info = state.layoutInfo
        val total = info.totalItemsCount
        val visible = info.visibleItemsInfo
        if (total == 0 || visible.isEmpty()) return@drawWithContent

        val itemSize = visible.first().size.coerceAtLeast(1)
        val itemsInView = size.height / itemSize
        val scrolledItems = state.firstVisibleItemIndex + state.firstVisibleItemScrollOffset.toFloat() / itemSize
        val maxScrolledItems = (total - itemsInView).coerceAtLeast(1f)
        val progress = if (state.canScrollForward) (scrolledItems / maxScrolledItems).coerceIn(0f, 1f) else 1f

        val thumbHeight = (size.height * itemsInView / total).coerceIn(16.dp.toPx(), size.height)
        val top = (size.height - thumbHeight) * progress
        val width = ScrollThumbWidth.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width - width, top),
            size = Size(width, thumbHeight),
            cornerRadius = CornerRadius(width / 2),
        )
    }

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun WeeklyScreenPreview() {
    val monday = LocalDate.of(2026, 11, 16)
    ReNoteTheme {
        WeeklyScreen(
            startWeek = monday,
            notesByDate = SampleNotes.between(monday, monday.plusDays(6)),
            today = LocalDate.of(2026, 11, 22),
            onVisibleWeekChange = {},
            onDayClick = {},
            onSwitchView = {},
        )
    }
}
