package com.example.callyourmother

import android.app.ListActivity
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class ContactsActivity: ListActivity() {
    lateinit var mAdapter: ContactsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contactslist)
        val gson = Gson()
        val json: String = intent.getStringExtra("contacts array") as String
        val type: Type = object : TypeToken<java.util.ArrayList<Contacts>?>() {}.type
        val contactList: ArrayList<Contacts> = gson.fromJson(json, type)
        mAdapter = ContactsAdapter(applicationContext,
            contactList
        ) //input the contacts array that was past through from intent into second parameter

        listAdapter = mAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
