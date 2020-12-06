package com.example.callyourmother

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.ImageDecoder.createSource
import android.graphics.ImageDecoder.decodeBitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.CallLog
import android.provider.ContactsContract
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Long
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.Throws


class RunInBackground : Service() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(
            1,
            Notification()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "example.permanence"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (prefs.getString("key", null) == null) {
            val contactArray: ArrayList<Contacts> = ArrayList<Contacts>()
            saveArray(contactArray)
        }

        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            addAllContacts()
        }


        val gson = Gson()
        val json: String = prefs.getString("key", null) as String
        val type: Type = object : TypeToken<java.util.ArrayList<Contacts>?>() {}.type
        val contactList: ArrayList<Contacts> = gson.fromJson(json, type)
        var group1 = prefs.getInt("Notif1", 1) //intent!!.getIntExtra("Group 1", 1)
        var group2 = prefs.getInt("Notif2", 5) //intent!!.getIntExtra("Group 2", 5)
        var group3 = prefs.getInt("Notif3", 10) //intent!!.getIntExtra("Group 3", 10)


        // TODO: For testing purposes, assign Group 1 -> true, and change a contact into group 1
        var mNotification : ArrayList<Contacts> = contactList.filter { contact: Contacts ->
            when (contact.notification) {
                "Group 1" -> (diffDates(contact.lastCallDate!!) < group1)
                "Group 2" -> (diffDates(contact.lastCallDate!!) < group2)
                "Group 3" -> (diffDates(contact.lastCallDate!!) < group3)
                else -> true
            }
        } as ArrayList<Contacts>

        //start notifications
        if (mNotification.isNotEmpty()) {
            startNotification(mNotification)
        }

        //Stops once command is done
        stopSelf()


        return START_STICKY
    }

    @Throws(UnsupportedOperationException::class)
    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    //TODO: For testing purpose, change the time delay to required time in milliseconds
    override fun onDestroy() {
        val time: kotlin.Long = 1000 * 60 * 60 *24
        super.onDestroy()
        val broadCastIntent = Intent().setAction("restartservice").setClass(this, Restarter::class.java)
        val mHandler = Handler()
        mHandler.postDelayed(Runnable(){
            sendBroadcast(broadCastIntent)
        },  time)

    }



    private fun startNotification(array : ArrayList<Contacts>) {
        var CHANNEL_ID : String
        var channelname : String
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var mChannel : NotificationChannel

            CHANNEL_ID = "notif_channel"
            channelname = "channel for notification"
            mChannel = NotificationChannel(CHANNEL_ID, channelname, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel)

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("You have not called " + array.size.toString() + " people in your contact")
            .setContentTitle("New Notification")


        //PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putBoolean("cleared", false).commit()
        val notificationIntent = Intent(this, NotificationActivity::class.java)
        //notificationIntent.putExtra("notifications array", array)
        val pendingIntent = PendingIntent.getActivity(applicationContext,0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT )
        builder.setContentIntent(pendingIntent)


        notificationManager.notify(0, builder.build())
    }

    private fun diffDates (date : Date) : Int {
        val cal : Date = Calendar.getInstance().time
        return (cal.year - date.year) * 365 + (cal.month - date.month) * 30 + (cal.day - date.day)
    }

    private fun addAllContacts(){
        // CREATE CURSOR TO MOVE THROUGH CONTACTS
        var cursor: Cursor = applicationContext.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,null,
            null, null) as Cursor

        // CHECK IF THERE ARE NO CONTACTS ON PHONE
        if (cursor.count == 0) {
            Toast.makeText(this, "No contacts", Toast.LENGTH_LONG).show()
            return
        }
        
        // PULL THE CURRENT CONTACT LIST FROM SHARED PREFERENCES
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json: String = prefs.getString("key", null) as String
        val type: Type = object : TypeToken<java.util.ArrayList<Contacts>?>() {}.type
        val useList: ArrayList<Contacts> = gson.fromJson(json, type)
        var contactList: ArrayList<Contacts> = ArrayList<Contacts>()

        // MOVE THROUGH ALL CONTACTS
        while (cursor.moveToNext()) {
            // GET IMAGE FROM CONTACT
            val imageURI = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
            val uri = Uri.parse(imageURI)
            val source = createSource(this.contentResolver, uri)
            val bitmap = decodeBitmap(source)

            // GET PHONE NUMBER FROM CONTACT
            val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            var phone = ""
            if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                val phoneCursor: Cursor = applicationContext.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf<String>(id),
                    null
                )!!
                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    phone =
                        phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phoneCursor.close()
                }
            }

            // GET NAME FROM CONTACT
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

            // GET NOTIFICATION GROUP
            var notificationGroup = "Group 2"
            for (check in useList) {
                if (check.phone == phone) {
                    notificationGroup = check.notification as String
                }
            }

            // GET DATE FROM CONTACT (HELPER FUNCTION)
            val date: Date = getDate(phone)

            // CREATE CONTACT WITH COLLECTED INFORMATION
            var contact = Contacts(bitmap, phone, name, notificationGroup, date)

            contactList.add(contact)
        }
        // SAVE THE ARRAY TO SHARED PREFERENCES (HELPER FUNCTION)
        saveArray(contactList)
    }

    // TAKES IN A NUMBER AND RETURNS THE MOST RECENT DATE THAT NUMBER WAS CALLED BY THE USER
    private fun getDate(phone: String): Date {
        // CREATE A CURSOR OF CALL LOGS
        val allCalls: Uri = Uri.parse("content://call_log/calls")
        val cursor: Cursor = applicationContext.contentResolver.query(allCalls, null,
            null, null, null) as Cursor

        // REMOVE PHONE NUMBER FORMATTING FROM GIVEN NUMBER
        var callDate = ""
        val re = Regex("-| |\\(|\\)")
        var newPhone = re.replace(phone, "")

        // CHECKER TO MAKE SURE MOST RECENT LOG ISN'T REPLACED BY OLDER LOG
        var notFound = true
        // ITERATE THROUGH LOGS UNTIL MATCH IS FOUND
        while (cursor.moveToNext() && notFound) {
            // MATCH IS FOUND IF THE PHONE NUMBER GIVEN IS THE PHONE NUMBER OF THE CURRENT LOG
            if (newPhone == cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))) {
                // GET THE DATE
                val date: Int = cursor.getColumnIndex(CallLog.Calls.DATE)
                callDate = cursor.getString(date)
                notFound = false
            }
        }
        // IF LOG IS NOT FOUND, RETURN FILLER DATE
        if (notFound) {
            return Date(1, 1, 1900)
        }
        return Date(Long.valueOf(callDate))
    }

    // SAVE THE ARRAY TO SHARED PREFERENCES USING JSON
    private fun saveArray(contactList: ArrayList<Contacts>) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var editor = prefs.edit()
        val gson = Gson()
        val jsonText = gson.toJson(contactList)
        editor.putString("key", jsonText)
        editor.commit()
    }

}
