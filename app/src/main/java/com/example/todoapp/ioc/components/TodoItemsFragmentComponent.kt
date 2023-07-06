package com.example.todoapp.ioc.components

import com.example.todoapp.data.TodoItemsRepository
import com.example.todoapp.fragments.TodoItemsFragment
import com.example.todoapp.ioc.util.MutableString
import com.example.todoapp.network.APIService
import dagger.Subcomponent

@Subcomponent
interface TodoItemsFragmentComponent {
    fun revision(): MutableString
    fun todoItemsRepository(): TodoItemsRepository
    fun apiService(): APIService
    fun inject(fragment: TodoItemsFragment)
}