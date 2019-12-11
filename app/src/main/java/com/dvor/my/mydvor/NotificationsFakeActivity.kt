package com.dvor.my.mydvor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class NotificationsFakeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_fake)

        val i = Intent(this, MainActivity::class.java)
        i.putExtra("fragment", "NotificationFragment")
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
    }
}