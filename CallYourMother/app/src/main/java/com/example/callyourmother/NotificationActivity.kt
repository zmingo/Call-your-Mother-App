package com.example.callyourmother

import android.app.ListActivity
import android.os.Bundle

class NotificationActivity: ListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notificationlist)
        var adapters = NotificationAdapter(applicationContext)
        listAdapter = adapters
    }
}