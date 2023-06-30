package com.example.todoapp.network.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.MyApp
import com.example.todoapp.data.TodoItem
import com.example.todoapp.data.TodoItemsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext


class BackgroundWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val request = withContext(Dispatchers.IO) {
                async { MyApp.instance.apiService.downloadTodoItems() }
            }

            val response = request.await()

            if (response.isSuccessful) {
                val list = response.body()!!.list
                TodoItemsRepository().updateList(list as ArrayList<TodoItem>)
            } else {
                return Result.retry()
            }
        } catch (error: Exception) {
            return Result.retry()
        }


        return Result.success()
    }
}