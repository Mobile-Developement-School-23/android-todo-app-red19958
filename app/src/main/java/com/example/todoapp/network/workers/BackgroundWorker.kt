package com.example.todoapp.network.workers


import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todoapp.MyApp
import com.example.todoapp.data.TodoItem
import com.example.todoapp.data.TodoItemsRepository
import com.example.todoapp.ioc.util.MutableString
import com.example.todoapp.network.APIService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BackgroundWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val todoItemsRepository: TodoItemsRepository,
    private val apiService: APIService,
    private val  revision: MutableString
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val request = withContext(Dispatchers.IO) {
                async { apiService.downloadTodoItems() }
            }

            val response = request.await()

            if (response.isSuccessful) {
                val list = response.body()!!.list
                todoItemsRepository.updateList(list as ArrayList<TodoItem>)
                Log.d("revisionBack", revision.toString())
                revision.set(response.body()!!.revision.toString())
            } else {
                return Result.retry()
            }
        } catch (error: Exception) {
            return Result.retry()
        }


        return Result.success()
    }
}