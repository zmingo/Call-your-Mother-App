package com.example.callyourmother

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

    //TODO get the contacts updated
    var mContacts = ArrayList<Contacts>() // gets the contact
    var mNotification = ArrayList<Contacts>()



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState != null) {
            NOTIF1 = savedInstanceState.getInt("notif1")
            NOTIF2 = savedInstanceState.getInt("notif2")
            NOTIF3 = savedInstanceState.getInt("notif3")
        }

        var contacts_button = findViewById<Button>(R.id.contacts_button)

        contacts_button.setOnClickListener {
            var intent = Intent(this, ContactsActivity::class.java)
            //TODO
            // put extra of array of contacts to be sent to contacts activity to be displayed
            intent.putExtra("contacts array", mContacts)

            startActivityForResult(intent,0)
        }

        mNotification = mContacts.filter { contact: Contacts ->
            when (contact.notification) {



            }




        }




        var notification_button = findViewById<Button>(R.id.notifications_button)
        notification_button.setOnClickListener {
            var intent = Intent(this, NotificationActivity::class.java)
            //TODO
            // put extra of array of notifications to be sent to notifications activity to be displayed
            intent.putExtra("notification array", mNotification)

            startActivity(intent)
        }

        // Starts the service for running in background
        val intent : Intent = Intent()
        intent.putExtra("Contacts array", mContacts)
        intent.putExtra("Notification Array", mNotification)
        intent.setClass(applicationContext, RunInBackground::class.java)
        startService(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        menu.add(Menu.NONE, 1, Menu.NONE, "Clear Notifications")
        menu.add(Menu.NONE, 2, Menu.NONE, "Edit Notification Groups")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                //TODO
                // Delete all past notifications in stored notifications array
                mNotification.clear()
                return true
            }

            2 -> {
                var dialogBuilder = AlertDialog.Builder(this)
                var ndialog = layoutInflater.inflate(
                    R.layout.notificationgroupdialog,
                    null
                )  //Custom Dialog for entering number of days per Notification group

                //TODO
                // EditText fields where they can enter num of days. Fill the text with existing days if there are previously saved settings, if not make the default 1, 5, 10 days
                var notif1 = findViewById<EditText>(R.id.notification1)
                var notif2 = findViewById<EditText>(R.id.notification2)
                var notif3 = findViewById<EditText>(R.id.notification3)


                dialogBuilder.setView(ndialog)

                val title = TextView(this) //Title bar styling for dialog
                title.text = "Notification Group Settings"
                title.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                title.setPadding(20, 20, 20, 20)
                title.gravity = Gravity.CENTER
                title.setTextColor(Color.WHITE)
                title.textSize = 20f
                dialogBuilder.setCustomTitle(title)

                var dialog = dialogBuilder.create() //Create and display the dialog to the user
                dialog.show()

                var saveButton = findViewById<Button>(R.id.saveButton)
                saveButton.setOnClickListener {
                    //TODO
                    // Edit notification groups and backend monitoring
                    NOTIF1 = notif1.toString().toInt()
                    NOTIF2 = notif2.toString().toInt()
                    NOTIF3 = notif3.toString().toInt()

                }

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //TODO get result from contacts activity and update contacts array with notification group changes (if any)


        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outPersistentState.putInt("Notif1", NOTIF1)
        outPersistentState.putInt("Notif2", NOTIF2)
        outState.putInt("Notif3", NOTIF3)

        super.onSaveInstanceState(outState, outPersistentState)
    }

    companion object {
        var NOTIF1 = 1
        var NOTIF2 = 5
        var NOTIF3 = 10
    }
}