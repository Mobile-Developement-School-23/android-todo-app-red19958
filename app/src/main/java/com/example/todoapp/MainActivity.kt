package com.example.todoapp

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.fragments.util.Const.CHANNEL_DESCRIPTION
import com.example.todoapp.fragments.util.Const.CHANNEL_ID
import com.example.todoapp.fragments.util.Const.CHANNEL_NAME
import com.example.todoapp.fragments.util.Const.GROUP_ID
import com.example.todoapp.fragments.util.Const.GROUP_NAME
import com.example.todoapp.fragments.util.Const.MY_PREFS
import com.example.todoapp.fragments.util.Const.NOTIFICATION_KEY
import com.example.todoapp.fragments.util.Const.THEME_KEY


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var bundle: Bundle? = null
    private lateinit var sharedPref: SharedPreferences
    private var theme = 2
    private var checkedNotification = false
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref = applicationContext.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        theme = sharedPref.getInt(THEME_KEY, 2)
        checkedNotification = sharedPref.getBoolean(NOTIFICATION_KEY, false)
        editor = sharedPref.edit()

        when (theme) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!checkedNotification) {
            checkNotificationPermission()
            checkedNotification = true
            editor.putBoolean(NOTIFICATION_KEY, true)
            editor.apply()
        }


        if (notificationManager.areNotificationsEnabled()) {

            notificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(
                    GROUP_ID,
                    GROUP_NAME
                )
            )

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                group = GROUP_ID
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK)
                checkNotificationPermission()
        }

    private fun checkNotificationPermission() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.areNotificationsEnabled()) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            notificationPermissionLauncher.launch(intent)
        }
    }

    fun getBundle(): Bundle? {
        return bundle
    }

    fun setBundle(bundle: Bundle) {
        this.bundle = bundle
    }
}