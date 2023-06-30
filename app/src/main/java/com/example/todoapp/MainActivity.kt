package com.example.todoapp

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import com.example.todoapp.data.TodoItem
import com.example.todoapp.data.TodoItemsRepository
import com.example.todoapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var bundle: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycle.coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val list = MyApp.instance.todoItemDAO.getAllData()
                TodoItemsRepository().updateList(list as ArrayList<TodoItem>)
            }
        }
    }

    fun getBundle(): Bundle? {
        return bundle
    }

    fun setBundle(bundle: Bundle) {
        this.bundle = bundle
    }

    fun replaceItemDone(done: Boolean, id: String){
        lifecycle.coroutineScope.launch { TodoItemsRepository().replaceItemDone(done, id) }
    }

    fun removeItem(item: TodoItem){
        lifecycle.coroutineScope.launch { TodoItemsRepository().removeItem(item) }
    }

}