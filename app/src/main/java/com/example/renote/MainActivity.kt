package com.example.renote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.renote.data.Note
import com.example.renote.ui.daily.DailyScreen
import com.example.renote.ui.daily.SampleNotes
import com.example.renote.ui.detail.DetailScreen
import com.example.renote.ui.detail.DetailUiState
import com.example.renote.ui.theme.ReNoteTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReNoteTheme {
                StaticHost()
            }
        }
    }
}

/**
 * Temporary host for build order steps 2–3: the Daily view over hard-coded notes, and the
 * Item detail screen opened from it. Nothing is persisted — Save and Delete just return to
 * Daily. Replaced by the NavGraph and ViewModels in step 4.
 */
@Composable
private fun StaticHost() {
    // The day the pager is centred on; moved to the visible day when Detail opens, so
    // returning to Daily lands on the day the user left rather than on today.
    var anchorDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var visibleDate by rememberSaveable { mutableStateOf(anchorDate) }
    var detail by remember { mutableStateOf<DetailUiState?>(null) }
    val notesByDate = remember(visibleDate) { SampleNotes.around(visibleDate) }

    val openDetail = { state: DetailUiState ->
        anchorDate = visibleDate
        detail = state
    }
    val closeDetail = { detail = null }

    val current = detail
    if (current == null) {
        DailyScreen(
            startDate = anchorDate,
            notesByDate = notesByDate,
            showBack = false,
            onVisibleDateChange = { visibleDate = it },
            onBack = {},
            onAddNote = { date -> openDetail(DetailUiState(date = date, isNew = true)) },
            onNoteClick = { id ->
                notesByDate.values.flatten().firstOrNull { it.id == id }
                    ?.let { openDetail(it.toDetailUiState()) }
            },
            onSwitchView = {},
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        // The system back button is equivalent to Cancel (spec §6.4).
        BackHandler(onBack = closeDetail)
        DetailScreen(
            state = current,
            onTitleChange = { detail = detail?.copy(title = it) },
            onDescriptionChange = { detail = detail?.copy(description = it) },
            onTimeChange = { detail = detail?.copy(time = it) },
            onSave = closeDetail,
            onCancel = closeDetail,
            onDeleteClick = { detail = detail?.copy(isDeleteConfirmVisible = true) },
            onDeleteConfirm = closeDetail,
            onDeleteDismiss = { detail = detail?.copy(isDeleteConfirmVisible = false) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun Note.toDetailUiState() = DetailUiState(
    date = date,
    isNew = false,
    title = title,
    description = description,
    time = time,
)
