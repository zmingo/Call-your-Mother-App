package com.example.callyourmother

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.*
import kotlin.collections.ArrayList

class NotificationAdapter(Context: Context, Array: ArrayList<Contacts>) : BaseAdapter()  {

    private val array = Array

    override fun getCount(): Int {
        return array.size
    }

    override fun getItem(p0: Int): Contacts {
        return array[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        var view = p1
        val contacts = getItem(p0)
        if (view == null) {
            view = inflate!!.inflate(R.layout.notifications, p2, false)
        }

        val lastCalledOn = view?.findViewById(R.id.lastcalledon) as TextView
        val phone = view.findViewById(R.id.num) as TextView
        val name = view.findViewById(R.id.callName) as TextView
        val notification = view.findViewById(R.id.notification) as TextView
        phone.text = contacts.phone
        name.text = contacts.name
        notification.text = contacts.notification

        //Checks if the person has never been called before and displays text instead of default date
        if (contacts.lastCallDate == Date(1,1,1900)) {
            lastCalledOn.text = "Never called before"
        }
        else
            lastCalledOn.text = contacts.lastCallDate.toString()

        return view
    }

    companion object {
        private var inflate: LayoutInflater? = null
    }

    init {
        inflate = LayoutInflater.from(Context)
    }


}