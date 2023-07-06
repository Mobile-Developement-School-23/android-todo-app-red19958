package com.example.todoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todoapp.data.util.Converter

@Database(entities = [TodoItem::class], version = 1)
@TypeConverters(Converter::class)
abstract class TodoItemDatabase : RoomDatabase() {
    abstract fun todoItemDao(): TodoItemDAO

    /*companion object {
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
    }*/
}