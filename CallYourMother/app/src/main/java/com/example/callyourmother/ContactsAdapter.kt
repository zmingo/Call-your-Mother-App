package com.example.callyourmother

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*


class ContactsAdapter(Context: Context, Array: ArrayList<Contacts>) : BaseAdapter() {

    private var array = Array
    private var context = Context

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
        var contacts = getItem(p0)
        if (view == null) {
            view = inflate!!.inflate(R.layout.contacts_item, p2, false);
        }

        var image = view?.findViewById(R.id.ContactPicture) as ImageView
        var phone = view.findViewById(R.id.num) as TextView
        var name = view.findViewById(R.id.Name) as TextView
        var notification = view.findViewById(R.id.notification) as Spinner

        image.setImageBitmap(contacts.image)
        phone.text = contacts.phone
        name.text = contacts.name

        if (contacts.notification.equals("Group 1")) {
            notification.setSelection(0)
        } else if (contacts.notification.equals("Group 2")) {
            notification.setSelection(1)
        } else {
            notification.setSelection(2)
        }

        notification.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                var selected = parent.adapter.getItem(position) as String
                contacts.notification = selected
                notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

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

    fun getArray(): ArrayList<Contacts> {
        return array
    }

    companion object {
        private var inflate: LayoutInflater? = null
    }

    init {
        inflate = LayoutInflater.from(Context)
    }

}