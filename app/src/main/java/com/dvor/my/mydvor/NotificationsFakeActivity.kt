package com.dvor.my.mydvor

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class NotificationsFakeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_fake)

        val i: Intent = Intent(this, MainActivity::class.java)
        i.putExtra("fragment", "NotificationFragment")
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
    }
}