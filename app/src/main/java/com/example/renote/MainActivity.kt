package com.example.renote

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.example.renote.ui.daily.DailyScreen
import com.example.renote.ui.daily.SampleNotes
import com.example.renote.ui.theme.ReNoteTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReNoteTheme {
                StaticDailyHost()
            }
        }
    }
}

/**
 * Temporary host for build order step 2: drives the Daily view from hard-coded notes.
 * Replaced by the NavGraph and DailyViewModel in step 4.
 */
@Composable
private fun StaticDailyHost() {
    val startDate = rememberSaveable { LocalDate.now() }
    var visibleDate by rememberSaveable { mutableStateOf(startDate) }
    val notesByDate = remember(visibleDate) { SampleNotes.around(visibleDate) }

    DailyScreen(
        startDate = startDate,
        notesByDate = notesByDate,
        showBack = false,
        onVisibleDateChange = { visibleDate = it },
        onBack = {},
        onAddNote = {},
        onNoteClick = {},
        onSwitchView = {},
        modifier = Modifier.fillMaxSize(),
    )
}
