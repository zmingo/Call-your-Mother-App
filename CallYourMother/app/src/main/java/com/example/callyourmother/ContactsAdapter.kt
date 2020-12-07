package com.example.callyourmother

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.Gson


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

        var bitmap: Bitmap? = null
        // https://developer.android.com/reference/android/graphics/ImageDecoder#createSource(android.content.ContentResolver,%20android.net.Uri)
        if (contacts.image != null) {
            val uri = Uri.parse(contacts.image)
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            bitmap = ImageDecoder.decodeBitmap(source)
        }
        // https://stackoverflow.com/questions/15255611/how-to-convert-a-drawable-image-from-resources-to-a-bitmap/15255786
        else {
            val d: Drawable = context.resources.getDrawable(R.drawable.iconfinder_contacts_309089)
            bitmap = Bitmap.createBitmap(
                50,
                50,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            d.setBounds(0, 0, canvas.width, canvas.height)
            d.draw(canvas)
        }

        image.setImageBitmap(bitmap)
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
                saveArray(array)
                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("cleared", false).commit()
                notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        return view
    }

    // https://stackoverflow.com/questions/38892519/store-custom-arraylist-in-sharedpreferences-and-get-it-from-there
    private fun saveArray(contactList: ArrayList<Contacts>) {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var editor = prefs.edit()
        val gson = Gson()
        var jsonText: String? = gson.toJson(contactList)

        editor.putString("key", jsonText)
        editor.commit()
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
