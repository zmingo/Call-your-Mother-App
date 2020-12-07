package com.example.callyourmother

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build


class Restarter : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast from
        // runinbackground class or the main activity when the app is closed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, RunInBackground::class.java))
        } else {
            context.startService(Intent(context, RunInBackground::class.java))
        }
    }
}
