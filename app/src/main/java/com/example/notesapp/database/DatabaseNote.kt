package com.example.notesapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notesapp.dao.NoteDao
import com.example.notesapp.entities.Note

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class DatabaseNote : RoomDatabase() {

    companion object {
        var notesDatabase: DatabaseNote? = null

        @Synchronized
        fun getDatabase(context: Context): DatabaseNote {
            if (notesDatabase == null) {
                notesDatabase = Room.databaseBuilder(
                    context, DatabaseNote::class.java, "notes.db"
                ).build()
            }
            return notesDatabase!!
        }
    }

    abstract fun noteDao(): NoteDao
}