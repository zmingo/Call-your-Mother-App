package com.example.callyourmother

import android.app.ListActivity
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception
import java.lang.reflect.Type


class ContactsActivity: ListActivity() {
    lateinit var mAdapter: ContactsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contactslist)
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json: String = prefs.getString("key", null) as String
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
