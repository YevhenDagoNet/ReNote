package com.example.renote.ui.detail

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.renote.R
import com.example.renote.data.Note
import com.example.renote.ui.common.ConfirmDialog
import com.example.renote.ui.common.rememberTimeFormatter
import com.example.renote.ui.theme.ReNoteTheme
import java.time.LocalDate
import java.time.LocalTime

/** The title counter appears once the title is this close to [Note.TITLE_MAX_LENGTH]. */
private const val TITLE_COUNTER_THRESHOLD = Note.TITLE_MAX_LENGTH - 20

/**
 * Item detail screen (spec §6.4), used for both creating and editing a note.
 * Stateless: every edit goes out through a callback and comes back in [state].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTimeChange: (LocalTime?) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isNew) R.string.detail_title_new else R.string.detail_title_edit
                        )
                    )
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = stringResource(R.string.detail_delete),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            ActionButtons(
                canSave = state.canSave,
                onSave = onSave,
                onCancel = onCancel,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TitleField(title = state.title, onTitleChange = onTitleChange)
            TimeField(time = state.time, onTimeChange = onTimeChange)
            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text(stringResource(R.string.detail_field_description)) },
                minLines = 5,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (state.isDeleteConfirmVisible) {
        ConfirmDialog(
            message = stringResource(R.string.detail_delete_confirm),
            onConfirm = onDeleteConfirm,
            onDismiss = onDeleteDismiss,
        )
    }
}

/** Input beyond [Note.TITLE_MAX_LENGTH] is rejected at the field, never accepted then flagged. */
@Composable
private fun TitleField(
    title: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = title,
        onValueChange = { if (it.length <= Note.TITLE_MAX_LENGTH) onTitleChange(it) },
        label = { Text(stringResource(R.string.detail_field_title)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        supportingText = if (title.length >= TITLE_COUNTER_THRESHOLD) {
            {
                Text(
                    text = stringResource(
                        R.string.detail_title_counter,
                        title.length,
                        Note.TITLE_MAX_LENGTH,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }
        } else {
            null
        },
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * "+ Add time" while no time is set; once set, the time itself (tap to change it) followed by
 * a small control that removes it again.
 */
@Composable
private fun TimeField(
    time: LocalTime?,
    onTimeChange: (LocalTime?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val formatter = rememberTimeFormatter()
    val openPicker = { showTimePicker(context, initial = time ?: defaultPickerTime(), onTimeChange) }

    if (time == null) {
        TextButton(onClick = openPicker, modifier = modifier) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.detail_add_time))
        }
    } else {
        val formatted = time.format(formatter)
        val changeDescription = stringResource(R.string.detail_change_time, formatted)
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = openPicker,
                modifier = Modifier.semantics { contentDescription = changeDescription },
            ) {
                Text(formatted, style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = { onTimeChange(null) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.detail_clear_time),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    canSave: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.action_cancel))
        }
        Button(onClick = onSave, enabled = canSave, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.action_save))
        }
    }
}

/** The picker opens at the current hour and minute when no time is set yet. */
private fun defaultPickerTime(): LocalTime = LocalTime.now().withSecond(0).withNano(0)

/** The platform's own time picker, in the device's 12/24h format (spec §6.4). */
private fun showTimePicker(context: Context, initial: LocalTime, onPicked: (LocalTime) -> Unit) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onPicked(LocalTime.of(hour, minute)) },
        initial.hour,
        initial.minute,
        DateFormat.is24HourFormat(context),
    ).show()
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenNewPreview() {
    ReNoteTheme {
        DetailScreen(
            state = DetailUiState(date = LocalDate.of(2026, 11, 22), isNew = true),
            onTitleChange = {},
            onDescriptionChange = {},
            onTimeChange = {},
            onSave = {},
            onCancel = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenEditPreview() {
    ReNoteTheme {
        DetailScreen(
            state = DetailUiState(
                date = LocalDate.of(2026, 11, 22),
                isNew = false,
                title = "Dentist appointment",
                description = "Clinic on Main Street, bring the insurance card.",
                time = LocalTime.of(9, 30),
            ),
            onTitleChange = {},
            onDescriptionChange = {},
            onTimeChange = {},
            onSave = {},
            onCancel = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
        )
    }
}
