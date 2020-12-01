package com.example.callyourmother

import android.Manifest
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.Long
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var contacts_button = findViewById(R.id.contacts_button) as Button
        contacts_button.setOnClickListener {
            //  requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
            //  requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG), 1)
            var intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }

        var notification_button = findViewById(R.id.notifications_button) as Button
        notification_button.setOnClickListener {
            var intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }
    }
    
    fun addAllContacts(): ArrayList<Contacts> {
        val contactList = ArrayList<Contacts>()
        var cursor: Cursor = applicationContext.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,null,
            null, null) as Cursor
        while (cursor.moveToNext()) {
            val fileName = ContactsContract.Contacts.PHOTO_FILE_ID
            val file = File(fileName)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)

            val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            var phone: String = ""
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
            val name =
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            val date: Date = getDate(phone)
            Toast.makeText(this, date.toString(), Toast.LENGTH_LONG).show()
            var contact = Contacts(bitmap, phone, name, "YAY", date)

            contactList.add(contact)
        }
        return contactList
    }

    fun getDate(phone: String): Date {
        val allCalls: Uri = Uri.parse("content://call_log/calls")
        val cursor = applicationContext.contentResolver.query(allCalls, null,
            null, null, null) as Cursor
        var callDate = ""
        val re = Regex("-| |\\(|\\)")
        var newPhone = re.replace(phone, "")
        while (cursor.moveToNext()) {
            if (newPhone == cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))) {
                val date: Int = cursor.getColumnIndex(CallLog.Calls.DATE)
                callDate = cursor.getString(date)
            }
        }
        return Date(Long.valueOf(callDate))
    }
}
