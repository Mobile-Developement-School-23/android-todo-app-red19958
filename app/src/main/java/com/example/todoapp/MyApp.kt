package com.example.todoapp

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.todoapp.data.TodoItem
import com.example.todoapp.fragments.util.Const
import com.example.todoapp.fragments.util.Const.BACKGROUND_WORKER
import com.example.todoapp.fragments.util.Const.CHANGE_INTENT
import com.example.todoapp.fragments.util.Const.CREATE
import com.example.todoapp.fragments.util.Const.DELETE
import com.example.todoapp.fragments.util.Const.ID
import com.example.todoapp.fragments.util.Const.IMPORTANCE
import com.example.todoapp.fragments.util.Const.REPEAT_TIME
import com.example.todoapp.fragments.util.Const.RU
import com.example.todoapp.fragments.util.Const.TEXT
import com.example.todoapp.fragments.util.Const.TIME_KEY
import com.example.todoapp.ioc.components.AppComponent
import com.example.todoapp.ioc.components.DaggerAppComponent
import com.example.todoapp.ioc.modules.AppModule
import com.example.todoapp.network.workers.BackgroundWorker
import com.example.todoapp.network.workers.SynchronizedWorkerFactory
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MyApp : Application() {
    lateinit var appComponent: AppComponent
        private set

    @Inject
    lateinit var workerFactory: SynchronizedWorkerFactory

    private lateinit var sharedPref: SharedPreferences

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

    fun checkAndSetNotification(item: TodoItem) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(
                    Const.GROUP_ID,
                    Const.GROUP_NAME
                )
            )

            val channel = NotificationChannel(
                Const.CHANNEL_ID,
                Const.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.apply {
                description = Const.CHANNEL_DESCRIPTION
                enableVibration(true)
                group = Const.GROUP_ID
            }

            notificationManager.createNotificationChannel(channel)

            if (item.deadline != null && !item.done)
                setNotification(item)
        }

    }

    private fun setNotification(item: TodoItem) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationBroadcastReceiver::class.java)
        intent.putExtra(CHANGE_INTENT, CREATE)
        intent.putExtra(ID, item.id)
        intent.putExtra(TEXT, item.text)
        intent.putExtra(IMPORTANCE, item.importance.toString())

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)
        val calendar: Calendar = Calendar.getInstance()
        sharedPref = applicationContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val time = sharedPref.getString(TIME_KEY, getString(R.string.midnight))
        val parts = time!!.split(':')

        val hour = if (parts[0][0] != '0') {
            parts[0].toInt()
        } else {
            parts[0][1].code - '0'.code
        }

        val minute = if (parts[1][0] != '0') {
            parts[1].toInt()
        } else {
            parts[1][1].code - '0'.code
        }

        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.YEAR, item.deadline!!.year)
        calendar.set(Calendar.MONTH, item.deadline!!.monthValue - 1)
        calendar.set(Calendar.DAY_OF_MONTH, item.deadline!!.dayOfMonth)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    fun checkAndDeleteNotification(item: TodoItem) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.areNotificationsEnabled())
            deleteNotification(item)
    }

    private fun deleteNotification(item: TodoItem) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationBroadcastReceiver::class.java)
        intent.putExtra(ID, item.id)
        intent.putExtra(CHANGE_INTENT, DELETE)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)
    }
}