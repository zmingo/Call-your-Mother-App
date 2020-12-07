package com.example.callyourmother

import android.app.ListActivity
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class ContactsActivity: ListActivity() {
    private lateinit var mAdapter: ContactsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contactslist)

        //Obtain contacts from shared preferences and compiles into an array list to be displayed
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json: String = prefs.getString("key", null) as String
        val type: Type = object : TypeToken<java.util.ArrayList<Contacts>?>() {}.type
        val contactList: ArrayList<Contacts> = gson.fromJson(json, type)

        mAdapter = ContactsAdapter(applicationContext,
            contactList
        ) //input the contacts array to be displayed from shared preferences

        listAdapter = mAdapter
    }

}
