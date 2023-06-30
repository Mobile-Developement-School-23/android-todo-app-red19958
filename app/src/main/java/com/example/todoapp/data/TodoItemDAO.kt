package com.example.todoapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoItemDAO {
    @Query("SELECT * FROM todo_item ORDER BY id ASC")
    fun getAllData(): List<TodoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(toDoData: TodoItem)

    @Update
    fun updateItem(toDoData: TodoItem)

    @Delete
    fun deleteItem(toDoData: TodoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: MutableList<TodoItem>)
}