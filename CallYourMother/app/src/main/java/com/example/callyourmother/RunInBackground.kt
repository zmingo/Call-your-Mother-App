package com.example.callyourmother

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import java.lang.UnsupportedOperationException
import kotlin.jvm.Throws

class RunInBackground : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //TODO(Query contacts and check for last called)
        //TODO use the latest contacts list if applicable
        //Maybe a function

        checkContacts()


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

        // Runs everyday
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + (1000*60*60*24),
            PendingIntent.getService(applicationContext, 0,
                Intent(this, RunInBackground::class.java), 0)
        )
    }

    private fun checkContacts() {
        var intent : Intent = Intent()
        intent.putExtra("Get contacts", 1)
        startActivity(intent)
    }
}
