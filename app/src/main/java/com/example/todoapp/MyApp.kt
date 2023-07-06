package com.example.todoapp

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.todoapp.fragments.util.Const.BACKGROUND_WORKER
import com.example.todoapp.fragments.util.Const.REPEAT_TIME
import com.example.todoapp.fragments.util.Const.RU
import com.example.todoapp.ioc.components.AppComponent
import com.example.todoapp.ioc.components.DaggerAppComponent
import com.example.todoapp.ioc.modules.AppModule
import com.example.todoapp.network.workers.BackgroundWorker
import com.example.todoapp.network.workers.SynchronizedWorkerFactory
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MyApp : Application() {
    lateinit var appComponent: AppComponent
        private set

    @Inject
    lateinit var workerFactory: SynchronizedWorkerFactory

    private fun setAppLocale(context: Context, locale: Locale) {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        context.createConfigurationContext(configuration)
    }

    override fun onCreate() {
        super.onCreate()
        setAppLocale(this, Locale(RU))
        initInjection()
        initWorker()
    }

    private fun initWorker() {
        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

        WorkManager.initialize(this, config)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest =
            PeriodicWorkRequest.Builder(BackgroundWorker::class.java, REPEAT_TIME, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            BACKGROUND_WORKER,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    private fun initInjection() {
        appComponent = DaggerAppComponent.factory().create(this, AppModule(this))
        appComponent.inject(this)
    }
}