package com.example.callyourmother

import android.Manifest
import android.app.Notification
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle

import android.preference.PreferenceManager
import android.provider.CallLog
import android.provider.ContactsContract
import android.text.Editable

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
import java.io.File
import java.lang.Long
import java.lang.reflect.Type
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

var mContacts = ArrayList<Contacts>()
var mNotification = ArrayList<Contacts>()
lateinit var mPrefs : SharedPreferences

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addAllContacts()
        requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG), 1)
        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)



        var contacts_button = findViewById<Button>(R.id.contacts_button)

        contacts_button.setOnClickListener {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val json: String = prefs.getString("key", null) as String
            var intent = Intent(this, ContactsActivity::class.java)

            intent.putExtra("contacts array", json)
            startActivityForResult(intent,0)
        }

        // Assigning the mContacts for local use
        val prefsForContact: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val jsonForContact: String = prefsForContact.getString("key", null) as String
        val type: Type = object : TypeToken<java.util.ArrayList<Contacts>?>() {}.type
        val useList: ArrayList<Contacts> = gson.fromJson(jsonForContact, type)
        mContacts = useList
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        // creating notification array
        mNotification = mContacts.filter { contact: Contacts ->
            when (contact.notification) {
                "Group 1" -> (((LocalDate.now() as Date) - contact.lastCallDate).Int() < mPrefs.getInt("Notif1", 1))
                "Group 2" -> (((LocalDate.now() as Date) - contact.lastCallDate).Int() < mPrefs.getInt("Notif2", 5))
                "Group 3" -> (((LocalDate.now() as Date) - contact.lastCallDate).Int() < mPrefs.getInt("Notif3", 10))
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
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val json: String = prefs.getString("key", null) as String
        intent.putExtra("contacts array", json)
        intent.putExtra("Group 1", mPrefs.getInt("Notif1", 1))
        intent.putExtra("Group 2", mPrefs.getInt("Notif2", 5))
        intent.putExtra("Group 3", mPrefs.getInt("Notif3", 10))
        intent.setClass(applicationContext, RunInBackground::class.java)
        startService(intent)
    }

    private fun addAllContacts(){
        // PULL THE CURRENT CONTACT LIST FROM SHARED PREFERENCES
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json: String = prefs.getString("key", null) as String
        val type: Type = object : TypeToken<java.util.ArrayList<Contacts>?>() {}.type
        val useList: ArrayList<Contacts> = gson.fromJson(json, type)
        val contactList: ArrayList<Contacts> = useList

        // CREATE CURSOR TO MOVE THROUGH CONTACTS
        var cursor: Cursor = applicationContext.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,null,
            null, null) as Cursor

        // CHECK IF THERE ARE NO CONTACTS ON PHONE
        if (cursor.count == 0) {
            Toast.makeText(this, "No contacts", Toast.LENGTH_LONG).show()
            return
        }

        // MOVE THROUGH ALL CONTACTS
        while (cursor.moveToNext()) {
            // GET IMAGE FROM CONTACT
            val fileName = ContactsContract.Contacts.PHOTO_FILE_ID
            val file = File(fileName)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)

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

            // GET DATE FROM CONTACT (HELPER FUNCTION)
            val date: Date = getDate(phone)

            // CREATE CONTACT WITH COLLECTED INFORMATION
            var contact = Contacts(bitmap, phone, name, "Group 2", date)

            // CHECK IF CONTACT ALREADY EXISTS IN ARRAY LIST, ADD IF IT DOESN'T
            var duplicate = false
            for (check in contactList) {
                if (check.phone == contact.phone) {
                    duplicate = true
                }
            }
            if (!duplicate) {
                contactList.add(contact)
            }
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
                mNotification.clear()
                return true
            }

            2 -> {
                var dialogBuilder = AlertDialog.Builder(this)
                var ndialog = layoutInflater.inflate(
                    R.layout.notificationgroupdialog,
                    null
                )  //Custom Dialog for entering number of days per Notification group

                //TODO
                // EditText fields where they can enter num of days. Fill the text with existing days if there are previously saved settings, if not make the default 1, 5, 10 days
                var notif1 = findViewById<EditText>(R.id.notification1)
                var notif2 = findViewById<EditText>(R.id.notification2)
                var notif3 = findViewById<EditText>(R.id.notification3)

                notif1.hint = mPrefs.getInt("Notif1", 1).toString()
                notif2.hint = mPrefs.getInt("Notif2", 5).toString()
                notif3.hint = mPrefs.getInt("Notif3", 10).toString()

                if ("" == notif1.text.toString()) {
                        notif1.text = mPrefs.getInt("Notif1", 1).toString() as Editable
                }
                if ("" == notif2.text.toString()) {
                       notif2.text = mPrefs.getInt("Notif2",5).toString() as Editable
                }
                if ("" == notif3.text.toString()) {
                        notif3.text = mPrefs.getInt("notif3", 10).toString() as Editable
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

                var saveButton = findViewById<Button>(R.id.saveButton)
                saveButton.setOnClickListener {
                    // Edit notification groups and backend monitoring

                    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                    var editor = prefs.edit()

                    editor.putInt("Notif1", notif1.text.toString().toInt())
                    editor.putInt("Notif2", notif2.text.toString().toInt())
                    editor.putInt("Notif3", notif3.text.toString().toInt())
                    editor.commit()
                }

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }



}
