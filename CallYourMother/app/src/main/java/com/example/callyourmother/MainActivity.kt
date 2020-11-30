package com.example.callyourmother

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var contacts_button = findViewById(R.id.contacts_button) as Button
        contacts_button.setOnClickListener {
            var intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }

        var notification_button = findViewById(R.id.notifications_button) as Button
        notification_button.setOnClickListener {
            var intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }
    }
}