package com.example.todoapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.todoapp.data.util.Converter
import com.example.todoapp.fragments.util.Const.SMALL_CHANGED_AT
import com.example.todoapp.fragments.util.Const.SMALL_CREATED_AT
import com.example.todoapp.fragments.util.Const.SMALL_DEADLINE
import com.example.todoapp.fragments.util.Const.SMALL_DONE
import com.example.todoapp.fragments.util.Const.SMALL_ID
import com.example.todoapp.fragments.util.Const.SMALL_IMPORTANCE
import com.example.todoapp.fragments.util.Const.SMALL_LAST_UPDATED_BY
import com.example.todoapp.fragments.util.Const.SMALL_TEXT
import com.example.todoapp.fragments.util.Const.TODO_ITEM
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = TODO_ITEM)
@TypeConverters(Converter::class)
data class TodoItem(
    @SerializedName(SMALL_ID) @PrimaryKey val id: String,
    @SerializedName(SMALL_TEXT) @ColumnInfo(name = SMALL_TEXT) var text: String,
    @SerializedName(SMALL_IMPORTANCE) @ColumnInfo(name = SMALL_IMPORTANCE) var importance: Importance,
    @SerializedName(SMALL_DEADLINE) @ColumnInfo(name = SMALL_DEADLINE) var deadline: LocalDate?,
    @SerializedName(SMALL_DONE) @ColumnInfo(name = SMALL_DONE) var done: Boolean,
    @SerializedName(SMALL_CREATED_AT) @ColumnInfo(name = SMALL_CREATED_AT) val dateOfCreation: LocalDateTime = LocalDateTime.now(),
    @SerializedName(SMALL_CHANGED_AT) @ColumnInfo(name= SMALL_CHANGED_AT) var dateOfChanges: LocalDateTime,
    @SerializedName(SMALL_LAST_UPDATED_BY) @ColumnInfo(name = SMALL_LAST_UPDATED_BY) val lastUpdatedBy: String = "0"
)

