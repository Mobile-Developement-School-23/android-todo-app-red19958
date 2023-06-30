package com.example.todoapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todoapp.data.util.Converter
import com.example.todoapp.fragments.util.Const.DATABASE_NAME

@Database(entities = [TodoItem::class], version = 1)
@TypeConverters(Converter::class)
abstract class TodoItemDatabase : RoomDatabase() {
    abstract fun todoItemDao(): TodoItemDAO

    companion object {
        @Volatile
        private var INSTANCE: TodoItemDatabase? = null
        fun getDatabase(context: Context): TodoItemDatabase {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        TodoItemDatabase::class.java,
                        DATABASE_NAME
                    ).build()
                }

                return INSTANCE as TodoItemDatabase
            }
        }
    }
}