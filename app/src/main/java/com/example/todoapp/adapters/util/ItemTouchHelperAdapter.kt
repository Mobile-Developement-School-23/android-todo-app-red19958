package com.example.todoapp.adapters.util

interface ItemTouchHelperAdapter {
    fun onItemDismiss(position: Int)
    fun onItemDone(position: Int)
}