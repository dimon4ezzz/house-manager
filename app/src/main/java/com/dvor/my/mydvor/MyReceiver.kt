package com.dvor.my.mydvor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.startForegroundService(Intent(context, MyNotifications::class.java))
    }
}