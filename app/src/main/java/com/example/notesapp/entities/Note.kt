package com.example.notesapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import kotlinx.serialization.Serializable as KotlinSerializable

@Entity(tableName = "Notes")
@KotlinSerializable
class Note : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    @ColumnInfo(name = "title")
    var title: String? = null

    @ColumnInfo(name = "date_time")
    var dateTime: String? = null

    @ColumnInfo(name = "note_text")
    var noteText: String? = null

    @ColumnInfo(name = "color")
    var color: String? = null

    @ColumnInfo(name = "sql_based")
    var sqlBased: Boolean = true

    override fun toString(): String {
        return "$title : $dateTime"
    }
}
