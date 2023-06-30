package com.example.todoapp

interface ItemTouchHelperAdapter {
    fun onItemDismiss(position: Int)
    fun onItemDone(position: Int)
}