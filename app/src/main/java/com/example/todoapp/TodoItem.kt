package com.example.todoapp

data class TodoItem(
    val id: String,
    var text: String,
    var importance: Importance,
    var done: Boolean,
    val dateOfCreation: String,
    var deadline: String = "",
    var dateOfChanges: String = ""
)