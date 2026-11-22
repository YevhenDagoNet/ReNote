package com.example.renote.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.renote.appContainer
import com.example.renote.data.Note
import com.example.renote.data.NoteRepository
import com.example.renote.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * Backs both `detail/new?date=…` (no note id) and `detail/{noteId}`.
 * Edits stay in memory until Save; Cancel simply leaves, discarding them (spec §6.4).
 */
class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: NoteRepository,
) : ViewModel() {

    private val noteId: Long? = savedStateHandle.get<Long>(Routes.ARG_NOTE_ID)

    /** Only meaningful for a new note; an existing note keeps its own date. */
    private val newNoteDate: LocalDate? =
        savedStateHandle.get<String>(Routes.ARG_DATE)?.let(LocalDate::parse)

    /** The note as loaded, so an edit only replaces the fields this screen owns. */
    private var original: Note? = null

    /** Set once a write has started, so a double tap cannot save or delete twice. */
    private var isWriting = false

    private val _uiState = MutableStateFlow(DetailUiState(isNew = noteId == null, isLoading = noteId != null))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        if (noteId == null) {
            checkNotNull(newNoteDate) { "detail/new requires a date" }
        } else {
            viewModelScope.launch {
                val note = repository.getById(noteId)
                original = note
                _uiState.value = note?.let {
                    DetailUiState(isNew = false, title = it.title, description = it.description, time = it.time)
                } ?: DetailUiState(isNew = false, isClosed = true)
            }
        }
    }

    fun onTitleChange(title: String) {
        if (title.length <= Note.TITLE_MAX_LENGTH) _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onTimeChange(time: LocalTime?) {
        _uiState.update { it.copy(time = time) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave || isWriting) return
        isWriting = true
        viewModelScope.launch {
            val existing = original
            if (existing == null) {
                repository.create(
                    date = checkNotNull(newNoteDate),
                    title = state.title,
                    description = state.description,
                    time = state.time,
                )
            } else {
                repository.update(
                    existing.copy(title = state.title, description = state.description, time = state.time)
                )
            }
            _uiState.update { it.copy(isClosed = true) }
        }
    }

    fun onDeleteClick() {
        if (original != null) _uiState.update { it.copy(isDeleteConfirmVisible = true) }
    }

    fun onDeleteDismiss() {
        _uiState.update { it.copy(isDeleteConfirmVisible = false) }
    }

    fun onDeleteConfirm() {
        val existing = original ?: return
        if (isWriting) return
        isWriting = true
        viewModelScope.launch {
            repository.delete(existing)
            _uiState.update { it.copy(isDeleteConfirmVisible = false, isClosed = true) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DetailViewModel(createSavedStateHandle(), appContainer.noteRepository)
            }
        }
    }
}
