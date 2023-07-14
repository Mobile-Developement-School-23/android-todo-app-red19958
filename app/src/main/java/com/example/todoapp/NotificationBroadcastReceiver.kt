package com.example.todoapp

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.todoapp.fragments.util.Const.CHANGE_INTENT
import com.example.todoapp.fragments.util.Const.CHANNEL_ID
import com.example.todoapp.fragments.util.Const.CREATE
import com.example.todoapp.fragments.util.Const.DELETE
import com.example.todoapp.fragments.util.Const.ID
import com.example.todoapp.fragments.util.Const.IMPORTANCE
import com.example.todoapp.fragments.util.Const.TEXT


class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getStringExtra(CHANGE_INTENT)) {
            CREATE -> {
                val id = intent.getStringExtra(ID)
                val text = intent.getStringExtra(TEXT)
                val importance = intent.getStringExtra(IMPORTANCE)
                scheduleNotification(context, id!!, text!!, importance!!)
            }

            DELETE -> {
                val id = intent.getStringExtra(ID)
                cancelNotification(context, id!!)
            }
        }
    }

    private fun scheduleNotification(
        context: Context,
        id: String,
        text: String,
        importance: String
    ) {
        Log.d("notify", "norm")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var importanceForNotify = ""
        val array = context.resources.getStringArray(R.array.importances)

        when (importance) {
            "low" -> importanceForNotify = array[0]
            "basic" -> importanceForNotify = array[1]
            "important" -> importanceForNotify = array[2]
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.check)
            .setContentTitle(context.getString(R.string.notification))
            .setContentText("$importanceForNotify $text")
            .setChannelId(CHANNEL_ID)

        notificationManager.notify(id.toInt(), notificationBuilder.build())
    }

    private fun cancelNotification(context: Context, id: String) {
        Log.d("cancel notify", "cancel")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id.toInt())
    }
}