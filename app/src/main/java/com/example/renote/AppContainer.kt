package com.example.renote

import android.content.Context
import com.example.renote.data.NoteDatabase
import com.example.renote.data.NoteRepository

/** Manual dependency wiring (no DI framework in v1). One instance per process. */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: NoteDatabase by lazy { NoteDatabase.create(appContext) }

    val noteRepository: NoteRepository by lazy { NoteRepository(database.noteDao()) }
}
