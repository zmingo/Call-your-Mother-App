package com.example.callyourmother

import android.app.ListActivity
import android.os.Bundle
import android.widget.ListView

class ContactsActivity: ListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contactslist)
        var adapters = ContactsAdapter(applicationContext)

        listAdapter = adapters
    }
}