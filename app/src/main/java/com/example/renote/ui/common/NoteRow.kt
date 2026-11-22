package com.example.renote.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalTime

/** Target row height from spec §6.1: about 7 rows fit on a typical phone screen. */
val NoteRowMinHeight = 88.dp

/**
 * One note in the Daily list: optional time in a fixed leading column (so titles line up
 * whether or not a row has a time), then the title clamped to two lines.
 */
@Composable
fun NoteRow(
    title: String,
    time: LocalTime?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = rememberTimeFormatter()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = NoteRowMinHeight)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = time?.format(timeFormatter).orEmpty(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.width(72.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}
