package com.example.todoapp.ioc.modules

import android.app.Application
import androidx.room.Room
import com.example.todoapp.data.TodoItemDAO
import com.example.todoapp.data.TodoItemDatabase
import com.example.todoapp.fragments.util.Const.DATABASE_NAME
import com.example.todoapp.ioc.scopes.MyDatabaseModuleScope
import dagger.Module
import dagger.Provides

@Module
class MyDatabaseModule {
    @Provides
    @MyDatabaseModuleScope
    fun provideMyAppDatabase(application: Application): TodoItemDatabase {
        return Room.databaseBuilder(application, TodoItemDatabase::class.java, DATABASE_NAME).build()
    }

    @Provides
    @MyDatabaseModuleScope
    fun provideTodoItemDao(database: TodoItemDatabase): TodoItemDAO {
        return database.todoItemDao()
    }
}