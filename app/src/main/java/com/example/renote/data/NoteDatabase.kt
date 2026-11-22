package com.example.renote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Note::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        private const val DATABASE_NAME = "renote.db"

        /** Call once per process; the AppContainer holds the resulting instance. */
        fun create(context: Context): NoteDatabase =
            Room.databaseBuilder(context.applicationContext, NoteDatabase::class.java, DATABASE_NAME)
                .build()
    }
}
