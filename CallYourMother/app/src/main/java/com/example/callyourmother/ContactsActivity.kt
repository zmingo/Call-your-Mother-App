package com.example.callyourmother

import android.app.ListActivity
import android.os.Bundle
import kotlin.collections.ArrayList


class ContactsActivity: ListActivity() {
    lateinit var mAdapter: ContactsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contactslist)
        mAdapter = ContactsAdapter(applicationContext,
            intent.getSerializableExtra("contacts array") as ArrayList<Contacts>
        ) //input the contacts array that was past through from intent into second parameter


        listAdapter = mAdapter
    }

    override fun onDestroy() {
        super.onDestroy()


        //TODO
        // return the adapter's array on result intent
    }



}