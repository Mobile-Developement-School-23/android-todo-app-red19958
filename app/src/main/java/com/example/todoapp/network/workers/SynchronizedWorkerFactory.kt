package com.example.todoapp.network.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.todoapp.data.TodoItemsRepository
import com.example.todoapp.ioc.util.MutableString
import com.example.todoapp.network.APIService
import javax.inject.Inject

class SynchronizedWorkerFactory @Inject constructor(
    private val todoItemsRepository: TodoItemsRepository,
    private val apiService: APIService,
    private val revision: MutableString
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return BackgroundWorker(appContext, workerParameters, todoItemsRepository, apiService, revision)
    }
}