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
    suspend fun getAllData(): List<TodoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(toDoData: TodoItem)

    @Update
    suspend fun updateItem(toDoData: TodoItem)

    @Delete
    suspend fun deleteItem(toDoData: TodoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: MutableList<TodoItem>)
}