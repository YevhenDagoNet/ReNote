package com.example.renote.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.renote.R

/** Narrow segments keep the switcher from crowding the screen title. */
private val SegmentWidth = 48.dp

enum class ViewMode(@param:StringRes val label: Int, @param:StringRes val description: Int) {
    DAY(R.string.switcher_day, R.string.switcher_day_description),
    WEEK(R.string.switcher_week, R.string.switcher_week_description),
    MONTH(R.string.switcher_month, R.string.switcher_month_description),
}

/** The D/W/M segmented control shown in the top-right corner of every calendar view. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSwitcher(
    selected: ViewMode,
    onSelect: (ViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = ViewMode.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        modes.forEachIndexed { index, mode ->
            val description = stringResource(mode.description)
            SegmentedButton(
                selected = mode == selected,
                onClick = { if (mode != selected) onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                icon = {},
                modifier = Modifier
                    .width(SegmentWidth)
                    .semantics { contentDescription = description },
            ) {
                Text(stringResource(mode.label))
            }
        }
    }
}
