package com.example.renote.ui.detail

import java.time.LocalDate
import java.time.LocalTime

/**
 * Everything the Item detail screen (spec §6.4) renders. [date] is never shown or edited;
 * it is carried so Save / Cancel / Delete know which day to return to.
 */
data class DetailUiState(
    val date: LocalDate,
    val isNew: Boolean,
    val title: String = "",
    val description: String = "",
    val time: LocalTime? = null,
    val isDeleteConfirmVisible: Boolean = false,
) {
    /** Save is disabled while the mandatory title is blank. */
    val canSave: Boolean get() = title.isNotBlank()
}
