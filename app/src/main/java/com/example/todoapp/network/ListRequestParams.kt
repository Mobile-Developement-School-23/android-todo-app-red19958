package com.example.todoapp.network

import com.example.todoapp.data.TodoItem
import com.example.todoapp.fragments.util.Const.ELEMENT
import com.google.gson.annotations.SerializedName

data class ListRequestParams(
    @SerializedName(ELEMENT) val element: TodoItem
)

