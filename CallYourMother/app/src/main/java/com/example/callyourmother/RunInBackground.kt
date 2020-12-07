package com.example.callyourmother

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
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

// https://stackoverflow.com/a/52258125
// Used this logic to run this service indefinitely with some delay using a handler
class RunInBackground : Service() {
    private var UNIQUE_REQUEST_CODE = 0

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(
            1,
            Notification()
        )
    }

    // A custom class to run on the foreground so app can run without being killed by the android
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
        val prefs: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (prefs.getString("key", null) == null) {
            val contactArray: ArrayList<Contacts> = ArrayList()
            saveArray(contactArray)
        }

        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        ) {
            addAllContacts()
        }


        val gson = Gson()
        val json: String = prefs.getString("key", null) as String
        val type: Type = object : TypeToken<java.util.ArrayList<Contacts>?>() {}.type
        val contactList: ArrayList<Contacts> = gson.fromJson(json, type)
        val group1 = prefs.getInt("Notif1", 1)
        val group2 = prefs.getInt("Notif2", 5)
        val group3 = prefs.getInt("Notif3", 10)


        // For testing purposes, assign Group 1 -> true, and change a contact into group 1
        val mNotification: ArrayList<Contacts> = contactList.filter { contact: Contacts ->
            when (contact.notification) {
                "Group 1" -> (diffDates(contact.lastCallDate!!) > group1)
                "Group 2" -> (diffDates(contact.lastCallDate!!) > group2)
                "Group 3" -> (diffDates(contact.lastCallDate!!) > group3)
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

    //for testing purpose, change the time delay to required time in milliseconds
    //sends out a broadcast every day so it can run in the background when destroyed
    override fun onDestroy() {
        val time: kotlin.Long = 1000 * 60 * 60 * 24
        val broadCastIntent =
            Intent().setAction("restartservice").setClass(this, Restarter::class.java)
        val mHandler = Handler()
        mHandler.postDelayed({
            sendBroadcast(broadCastIntent)
        }, time)
        super.onDestroy()
    }



    // fires a notification based on number of people that user has not called
    private fun startNotification(array: ArrayList<Contacts>) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val mChannel: NotificationChannel

        val CHANNEL_ID: String = "notif_channel"
        val channelname: String = "channel for notification"
        mChannel = NotificationChannel(CHANNEL_ID, channelname, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(mChannel)

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("You have not called " + array.size.toString() + " people in your contact")
            .setContentTitle("New Notification")

        val backIntent = Intent(this, MainActivity::class.java)
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val notificationIntent = Intent(this, NotificationActivity::class.java)
        //val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(applicationContext)
        //stackBuilder.addNextIntentWithParentStack(notificationIntent)

        val pendingIntent = PendingIntent.getActivities(this, UNIQUE_REQUEST_CODE++,
            arrayOf(backIntent, notificationIntent), PendingIntent.FLAG_ONE_SHOT)
        builder.setContentIntent(pendingIntent)


        notificationManager.notify(0, builder.build())
    }

    private fun diffDates(date: Date): kotlin.Long {
        val cal : Date = Calendar.getInstance().time
        val diff: kotlin.Long = cal.time - date.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return hours / 24
    }

    private fun addAllContacts() {
        // https://stackoverflow.com/questions/12562151/android-get-all-contacts/41827064
        // CREATE CURSOR TO MOVE THROUGH CONTACTS
        val cursor: Cursor = applicationContext.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI, null,
            null, null
        ) as Cursor

        // CHECK IF THERE ARE NO CONTACTS ON PHONE
        if (cursor.count == 0) {
            Toast.makeText(this, "No contacts", Toast.LENGTH_LONG).show()
            return
        }
        // https://stackoverflow.com/questions/38892519/store-custom-arraylist-in-sharedpreferences-and-get-it-from-there
        // PULL THE CURRENT CONTACT LIST FROM SHARED PREFERENCES
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json: String = prefs.getString("key", null) as String
        val type: Type = object : TypeToken<java.util.ArrayList<Contacts>?>() {}.type
        val useList: ArrayList<Contacts> = gson.fromJson(json, type)
        val contactList: ArrayList<Contacts> = ArrayList()

        // https://stackoverflow.com/questions/12562151/android-get-all-contacts/41827064
        // MOVE THROUGH ALL CONTACTS
        while (cursor.moveToNext()) {
            // https://developer.android.com/reference/android/provider/ContactsContract
            // GET IMAGE FROM CONTACT
            val bitmap =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))

            // https://developer.android.com/reference/android/provider/ContactsContract
            // https://gist.github.com/srayhunter/47ab2816b01f0b00b79150150feb2eb2
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

            // https://developer.android.com/reference/android/provider/ContactsContract
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

            // https://developer.android.com/reference/android/provider/ContactsContract
            // GET DATE FROM CONTACT (HELPER FUNCTION)
            val date: Date = getDate(phone)

            // CREATE CONTACT WITH COLLECTED INFORMATION
            val contact = Contacts(bitmap, phone, name, notificationGroup, date)

            contactList.add(contact)
        }
        // SAVE THE ARRAY TO SHARED PREFERENCES (HELPER FUNCTION)
        saveArray(contactList)
    }

    // TAKES IN A NUMBER AND RETURNS THE MOST RECENT DATE THAT NUMBER WAS CALLED BY THE USER
    private fun getDate(phone: String): Date {
        // https://stackoverflow.com/questions/12562151/android-get-all-contacts/41827064
        // CREATE A CURSOR OF CALL LOGS
        val allCalls: Uri = Uri.parse("content://call_log/calls")
        val cursor: Cursor = applicationContext.contentResolver.query(
            allCalls, null,
            null, null, null
        ) as Cursor

        // REMOVE PHONE NUMBER FORMATTING FROM GIVEN NUMBER
        var callDate = ""
        val re = Regex("-| |\\(|\\)")
        val newPhone = re.replace(phone, "")

        // CHECKER TO MAKE SURE MOST RECENT LOG ISN'T REPLACED BY OLDER LOG
        var notFound = true
        // https://stackoverflow.com/questions/12562151/android-get-all-contacts/41827064
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
        // https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.js/-date/
        // IF LOG IS NOT FOUND, RETURN FILLER DATE
        if (notFound) {
            return Date(1, 1, 1900)
        }
        return Date(Long.valueOf(callDate))
    }

    // SAVE THE ARRAY TO SHARED PREFERENCES USING JSON
    // https://stackoverflow.com/questions/38892519/store-custom-arraylist-in-sharedpreferences-and-get-it-from-there
    private fun saveArray(contactList: ArrayList<Contacts>) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = prefs.edit()
        val gson = Gson()
        val jsonText = gson.toJson(contactList)
        editor.putString("key", jsonText)
        editor.commit()
    }
}
