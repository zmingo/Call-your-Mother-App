package com.example.callyourmother

import android.app.ListActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

class NotificationActivity: ListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notificationlist)

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val gson = Gson()
        val json: String = prefs.getString("key", null) as String
        val type: Type = object : TypeToken<java.util.ArrayList<Contacts>?>() {}.type
        val contactList: ArrayList<Contacts> = gson.fromJson(json, type)
        var group1 = prefs.getInt("Notif1", 1)//intent!!.getIntExtra("Group 1", 1)
        var group2 = prefs.getInt("Notif2", 5)//intent!!.getIntExtra("Group 2", 5)
        var group3 = prefs.getInt("Notif3", 10)//intent!!.getIntExtra("Group 3", 10)

        var mNotification : ArrayList<Contacts> = contactList.filter { contact: Contacts ->
            when (contact.notification) {
                "Group 1" -> (diffDates(contact.lastCallDate!!) > group1)
                "Group 2" -> (diffDates(contact.lastCallDate!!) > group2)
                "Group 3" -> (diffDates(contact.lastCallDate!!) > group3)
                else -> true
            }
        } as ArrayList<Contacts>

        if (callingActivity == null) {
            PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putBoolean("cleared", false).commit()
        }

        if (prefs.getBoolean("cleared", false)) {
            mNotification.clear()
        }

        var adapters = NotificationAdapter(applicationContext,   mNotification)
        listAdapter = adapters
    }

    // https://stackoverflow.com/questions/10690370/how-do-i-get-difference-between-two-dates-in-android-tried-every-thing-and-pos
    private fun diffDates (date : Date) : Long {
        val cal : Date = Calendar.getInstance().time
        val diff: Long = cal.time - date.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return hours / 24
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
