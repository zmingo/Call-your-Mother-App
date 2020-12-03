package com.example.callyourmother

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import java.lang.UnsupportedOperationException
import java.lang.reflect.Type
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.Throws


class RunInBackground : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //TODO(Query contacts and check for last called)
        val gson = Gson()
        val json: String = intent!!.getStringExtra("contacts array") as String
        val type: Type = object : TypeToken<java.util.ArrayList<Contacts>?>() {}.type
        val contactList: ArrayList<Contacts> = gson.fromJson(json, type)
        var group1 = intent!!.getIntExtra("Group 1", 1)
        var group2 = intent!!.getIntExtra("Group 2", 5)
        var group3 = intent!!.getIntExtra("Group 3", 10)

        var mNotification : ArrayList<Contacts> = contactList.filter { contact: Contacts ->
            when (contact.notification) {
                "Group 1" -> (((LocalDate.now() as Date) - contact.lastCallDate).Int() < group1)
                "Group 2" -> (((LocalDate.now() as Date) - contact.lastCallDate).Int() < group2)
                "Group 3" -> (((LocalDate.now() as Date) - contact.lastCallDate).Int() < group3)
                else -> true
            }
        } as ArrayList<Contacts>

        //populate a notification list that will be used to start notifications.

        //start notifications
        if (mNotification.isNotEmpty())
            startNotification(mNotification)

        //Stops once command is done
        stopSelf()


        return START_NOT_STICKY
    }

    @Throws(UnsupportedOperationException::class)
    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onDestroy() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Runs everyday, once 24 hours
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + (1000*60*60*24),
            PendingIntent.getService(applicationContext, 0,
                Intent(this, RunInBackground::class.java), 0)
        )
    }
    private fun startNotification(array : ArrayList<Contacts>) {
        val builder = NotificationCompat.Builder(applicationContext)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("You have " + array.size.toString() + " notifications")
            .setContentTitle("New Notification")

        val notificationIntent = Intent(this, NotificationActivity::class.java)
        notificationIntent.putExtra("notifications array", array)
        val pendingIntent = PendingIntent.getActivity(applicationContext,0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT )
        builder.setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, builder.build())
    }

}
