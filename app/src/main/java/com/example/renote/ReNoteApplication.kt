package com.example.renote

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras

class ReNoteApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** The process-wide [AppContainer], for use inside ViewModel factory initializers. */
val CreationExtras.appContainer: AppContainer
    get() = (checkNotNull(this[APPLICATION_KEY]) as ReNoteApplication).container
