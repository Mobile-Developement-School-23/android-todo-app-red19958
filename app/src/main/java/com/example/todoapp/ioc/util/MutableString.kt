package com.example.todoapp.ioc.util

import com.example.todoapp.fragments.util.Const.ZERO

class MutableString {
    var value: String = ZERO

    fun set(newValue: String) {
        value = newValue
    }

    fun get(): String {
        return value
    }
}