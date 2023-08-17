package com.example.todoapp.network

import com.example.todoapp.data.TodoItem
import com.example.todoapp.fragments.util.Const.LIST
import com.example.todoapp.fragments.util.Const.REVISION
import com.google.gson.annotations.SerializedName

data class ListResponseParams(
    @SerializedName(LIST) val list: MutableList<TodoItem>,
    @SerializedName(REVISION) val revision: Int
)