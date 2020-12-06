package com.example.callyourmother

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.CallLog
import android.provider.ContactsContract
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Long
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.ImageDecoder.createSource
import android.graphics.ImageDecoder.decodeBitmap

var mContacts = ArrayList<Contacts>()
var mNotification = ArrayList<Contacts>()
lateinit var mPrefs : SharedPreferences

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (prefs.getString("key", null) == null) {
            val contactArray: ArrayList<Contacts> = ArrayList<Contacts>()
            saveArray(contactArray)
        }

       if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS
                ), 1
            )
        }
        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions needed. Restart app and give permissions", Toast.LENGTH_LONG).show()
        }

        var contacts_button = findViewById<Button>(R.id.contacts_button)

        contacts_button.setOnClickListener {
            addAllContacts()
            val json: String = prefs.getString("key", null) as String
            var intent = Intent(this, ContactsActivity::class.java)

            intent.putExtra("contacts array", json)
            startActivityForResult(intent,0)
        }

        // Assigning the mContacts for local use
        val prefsForContact: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val gson = Gson()
        val jsonForContact: String = prefsForContact.getString("key", null) as String
        val type: Type = object : TypeToken<java.util.ArrayList<Contacts>?>() {}.type
        val useList: ArrayList<Contacts> = gson.fromJson(jsonForContact, type)
        mContacts = useList
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        // creating notification array

        mNotification = mContacts.filter { contact: Contacts ->
            when (contact.notification) {
                "Group 1" -> (diffDates(contact.lastCallDate!!) < mPrefs.getInt("Notif1", 1))
                "Group 2" -> (diffDates(contact.lastCallDate!!) < mPrefs.getInt("Notif2", 5))
                "Group 3" -> (diffDates(contact.lastCallDate!!) < mPrefs.getInt("Notif3", 10))
                else -> true
            }
        } as ArrayList<Contacts>


        var notification_button = findViewById<Button>(R.id.notifications_button)
        notification_button.setOnClickListener {
            var intent = Intent(this, NotificationActivity::class.java)
            intent.putExtra("notifications array", mNotification)
            startActivityForResult(intent, 0)
        }

        // Starts the service for running in background
        val intent : Intent = Intent()
        //intent.putExtra("Group 1", mPrefs.getInt("Notif1", 1))
        //intent.putExtra("Group 2", mPrefs.getInt("Notif2", 5))
        //intent.putExtra("Group 3", mPrefs.getInt("Notif3", 10))
        intent.setClass(applicationContext, RunInBackground::class.java)

        if (!isMyServiceRunning(RunInBackground::class.java))
            startService(intent)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        menu.add(Menu.NONE, 1, Menu.NONE, "Clear Notifications")
        menu.add(Menu.NONE, 2, Menu.NONE, "Edit Notification Groups")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                //Clearing notifications
                mPrefs.edit().putBoolean("cleared", true).commit()
                return true
            }

            2 -> {
                var dialogBuilder = AlertDialog.Builder(this)
                var ndialog = layoutInflater.inflate(
                    R.layout.notificationgroupdialog,
                    null
                )  //Custom Dialog for entering number of days per Notification group

                var notif1 = ndialog.findViewById<EditText>(R.id.notification1)
                var notif2 = ndialog.findViewById<EditText>(R.id.notification2)
                var notif3 = ndialog.findViewById<EditText>(R.id.notification3)

                notif1.hint = mPrefs.getInt("Notif1", 1).toString()
                notif2.hint = mPrefs.getInt("Notif2", 5).toString()
                notif3.hint = mPrefs.getInt("Notif3", 10).toString()

                if (notif1.text.isEmpty() || notif1.text.toString().toInt() == 0) {
                    notif1.setText(mPrefs.getInt("Notif1", 1).toString())
                }
                if (notif2.text.isEmpty() || notif2.text.toString().toInt() == 0) {
                    notif2.setText(mPrefs.getInt("Notif2", 5).toString())
                }
                if (notif3.text.isEmpty() || notif3.text.toString().toInt() == 0) {
                    notif3.setText(mPrefs.getInt("notif3", 10).toString())
                }


                dialogBuilder.setView(ndialog)

                val title = TextView(this) //Title bar styling for dialog
                title.text = "Notification Group Settings"
                title.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                title.setPadding(20, 20, 20, 20)
                title.gravity = Gravity.CENTER
                title.setTextColor(Color.WHITE)
                title.textSize = 20f
                dialogBuilder.setCustomTitle(title)

                var dialog = dialogBuilder.create() //Create and display the dialog to the user
                dialog.show()

                var saveButton = ndialog.findViewById<Button>(R.id.saveButton)
                saveButton.setOnClickListener {
                    // Edit notification groups and backend monitoring

                    val prefs: SharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(this)
                    var editor = prefs.edit()

                    var v1 = if (notif1.text.isEmpty() || notif1.text.toString().toInt() == 0) {
                        mPrefs.getInt("Notif1", 1).toString()
                    } else notif1.text.toString()
                    var v2 = if (notif2.text.isEmpty() || notif2.text.toString().toInt() == 0) {
                        mPrefs.getInt("Notif2", 5).toString()
                    } else notif2.text.toString()
                    var v3 = if (notif3.text.isEmpty() || notif3.text.toString().toInt() == 0) {
                        mPrefs.getInt("Notif3", 10).toString()
                    } else notif3.text.toString()

                    editor.putInt("Notif1", v1.toInt())
                    editor.putInt("Notif2", v2.toInt())
                    editor.putInt("Notif3", v3.toInt())
                    editor.commit()
                    dialog.dismiss()
                }

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun diffDates (date : Date) : Int {
        val cal : Date = Calendar.getInstance().time
        return (cal.year - date.year) * 365 + (cal.month - date.month) * 30 + (cal.day - date.day)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        stopService(Intent(applicationContext, RunInBackground::class.java))
        val broadcastIntent = Intent().setAction("restartservice").setClass(this, Restarter::class.java)
        sendBroadcast(broadcastIntent)
        mPrefs.edit().putBoolean("cleared", false).commit()
        super.onDestroy()
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.getClassName()) {
                return true
            }
        }
        return false
    }


}
