package com.example.callyourmother

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

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

    override fun getView(p0: Int, p1: View, p2: ViewGroup?): View {
        var view = p1
        var contacts = getItem(p0)
        if (view == null) {
            view = inflate!!.inflate(R.layout.notifications, p2, false);
        }

        var lastCalledOn = view.findViewById(R.id.lastcalledon) as TextView
        var phone = view.findViewById(R.id.num) as TextView
        var name = view.findViewById(R.id.callName) as TextView
        var notification = view.findViewById(R.id.notification) as TextView
        phone.text = contacts.phone
        name.text = contacts.name
        notification.text = contacts.notification
        lastCalledOn.text = contacts.lastCallDate.toString()

        return view
    }

    fun add(x: Contacts) {
        array.add(x)
        notifyDataSetChanged()
    }

    fun delete(x: Contacts) {
        array.remove(x)
        notifyDataSetChanged()
    }

    fun deleteAll() {
        array.clear()
        notifyDataSetChanged()
    }

    companion object {
        private var inflate: LayoutInflater? = null
    }

    init {
        inflate = LayoutInflater.from(Context)
    }


}