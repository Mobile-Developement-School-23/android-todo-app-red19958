package com.example.todoapp.ioc.components

import com.example.todoapp.data.TodoItemsRepository
import com.example.todoapp.fragments.SettingsFragment
import dagger.Subcomponent

@Subcomponent
interface SettingsFragmentComponent {
    fun todoItemsRepository(): TodoItemsRepository
    fun inject(fragment: SettingsFragment)
}