package com.example.todoapp.ioc.components

import com.example.todoapp.data.TodoItemsRepository
import com.example.todoapp.fragments.AddItemFragment
import dagger.Subcomponent

@Subcomponent
interface AddItemFragmentComponent {
    fun todoItemsRepository(): TodoItemsRepository
    fun inject(fragment: AddItemFragment)
}