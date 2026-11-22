package com.example.renote.ui.detail

import java.time.LocalTime

/**
 * Everything the Item detail screen (spec §6.4) renders. The note's date is not part of it:
 * it is never shown, and the ViewModel holds it for saving.
 */
data class DetailUiState(
    val isNew: Boolean,
    val title: String = "",
    val description: String = "",
    val time: LocalTime? = null,
    val isDeleteConfirmVisible: Boolean = false,
    /** An existing note is still being read from the database. */
    val isLoading: Boolean = false,
    /** Saved, deleted or not found: the screen should leave. */
    val isClosed: Boolean = false,
) {
    /** Save is disabled while the mandatory title is blank. */
    val canSave: Boolean get() = !isLoading && title.isNotBlank()
}
