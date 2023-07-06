package com.example.todoapp.network

import com.example.todoapp.data.TodoItem
import com.example.todoapp.fragments.util.Const.ELEMENT
import com.example.todoapp.fragments.util.Const.REVISION
import com.google.gson.annotations.SerializedName

data class ElementResponseParams(
    @SerializedName(ELEMENT) val element: TodoItem,
    @SerializedName(REVISION) val revision: Int
)