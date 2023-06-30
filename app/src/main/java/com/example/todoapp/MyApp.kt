package com.example.todoapp

import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todoapp.data.TodoItemDAO
import com.example.todoapp.data.TodoItemDatabase
import com.example.todoapp.fragments.util.Const.BACKGROUND_WORKER
import com.example.todoapp.fragments.util.Const.RU
import com.example.todoapp.fragments.util.Const.URL_API
import com.example.todoapp.fragments.util.Const.ZERO
import com.example.todoapp.network.APIService
import com.example.todoapp.network.util.LocalDateLongConverter
import com.example.todoapp.network.util.LocalDateTimeLongConverter
import com.example.todoapp.network.workers.BackgroundWorker
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import java.util.concurrent.TimeUnit

class MyApp : Application() {
    lateinit var apiService: APIService
        private set

    private lateinit var database: TodoItemDatabase
    lateinit var todoItemDAO: TodoItemDAO
    var revision = ZERO

    private fun setAppLocale(context: Context, locale: Locale) {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        context.createConfigurationContext(configuration)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setAppLocale(this, Locale(RU))
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeLongConverter())
            .registerTypeAdapter(LocalDate::class.java, LocalDateLongConverter())
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(APIService::class.java)
        database = TodoItemDatabase.getDatabase(this@MyApp)
        todoItemDAO = database.todoItemDao()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<BackgroundWorker>(8, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            BACKGROUND_WORKER,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    companion object {
        lateinit var instance: MyApp
            private set
    }
}