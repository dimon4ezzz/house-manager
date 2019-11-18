package com.dvor.my.mydvor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class MyNotifications : Service() {
    internal lateinit var notificationManager: NotificationManager
    private var notificationId = 2

    private lateinit var mAuth: FirebaseAuth
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    internal var userStreetId: String = ""
    internal var organizationId: String = ""
    internal var userBuildingId: String = ""
    internal var myRef2: DatabaseReference? = null
    internal var myRef3: DatabaseReference? = null
    internal var myRef4: DatabaseReference? = null
    internal var myRef5: DatabaseReference? = null
    internal var messageSnapshot: DataSnapshot? = null
    internal var listenerBuilding: ValueEventListener? = null
    internal var listenerMessages: ValueEventListener? = null
    internal var notificationsSnapshot: DataSnapshot? = null
    internal lateinit var notificationsRead: DataSnapshot
    internal var listenerNotifications: ValueEventListener? = null
    internal var listenerRead: ValueEventListener? = null

    internal var context: Context? = null

    private fun addEventListener(eventListener: MyEventListener) {
        eventListeners!!.add(eventListener)
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel("0", "My channel",
                NotificationManager.IMPORTANCE_LOW)
        channel.description = "My channel description"
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(false)
        notificationManager.createNotificationChannel(channel)

        val channel_1 = NotificationChannel("1", "Сообщения от УК",
                NotificationManager.IMPORTANCE_HIGH)
        channel_1.description = "My channel description"
        channel_1.enableLights(true)
        channel_1.lightColor = Color.RED
        channel_1.enableVibration(true)
        notificationManager.createNotificationChannel(channel_1)

        val channel_2 = NotificationChannel("2", "Уведомления для вашего дома",
                NotificationManager.IMPORTANCE_HIGH)
        channel_2.description = "My channel description"
        channel_2.enableLights(true)
        channel_2.lightColor = Color.RED
        channel_2.enableVibration(true)
        notificationManager.createNotificationChannel(channel_2)

        val notification = NotificationCompat.Builder(this, "0")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Здравствуйте, уважаемый пользователь")
                .setContentText("Вы будете получать push-уведомления от MyDvor")

        startForeground(1, notification.build())
        deleteNotification()

        if (eventListeners == null) {
            eventListeners = LinkedList()
        } else {
            eventListeners!!.clear()
        }

        mAuth = FirebaseAuth.getInstance()

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user == null) {
                notificationManager.cancelAll()
                stopSelf()
            }
        }

        mAuth.addAuthStateListener(mAuthListener!!)

        val myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.uid!!)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    userStreetId = dataSnapshot.child("street_id").value!!.toString()
                    userBuildingId = dataSnapshot.child("building_id").value!!.toString()

                    notifyEventListeners(MyEvent(this, Type.UpdateAddressID))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        this.addEventListener(object : MyEventListener {
            override fun processEvent(event: MyEvent) {
                if (event.source == null) {
                    return
                }

                when (event.type) {
                    Type.UpdateAddressID -> {

                        if (myRef2 != null) {
                            myRef2!!.removeEventListener(listenerBuilding!!)
                        }

                        myRef2 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId)
                                .child("buildings").child(userBuildingId).child("organization_id")

                        listenerBuilding = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                organizationId = dataSnapshot.value!!.toString()

                                notifyEventListeners(MyEvent(this, Type.UpdateOrganizationId))
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        }

                        myRef2!!.addValueEventListener(listenerBuilding!!)


                        if (myRef4 != null) {
                            myRef4!!.removeEventListener(listenerNotifications!!)
                        }

                        myRef4 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId)
                                .child("buildings").child(userBuildingId).child("notifications")

                        listenerNotifications = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                notificationsSnapshot = dataSnapshot

                                notifyEventListeners(MyEvent(this, Type.UpdateNotifications))
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        }

                        myRef4!!.addValueEventListener(listenerNotifications!!)
                    }

                    Type.UpdateOrganizationId -> {

                        if (myRef3 != null) {
                            myRef3!!.removeEventListener(listenerMessages!!)
                        }

                        myRef3 = FirebaseDatabase.getInstance().getReference("organization")
                                .child(organizationId).child("messages").child(mAuth.uid!!)

                        listenerMessages = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                messageSnapshot = dataSnapshot

                                notifyEventListeners(MyEvent(this, Type.UpdateUI))
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        }

                        myRef3!!.addValueEventListener(listenerMessages!!)
                    }

                    Type.UpdateNotifications -> {
                        if (myRef5 != null) {
                            myRef5!!.removeEventListener(listenerRead!!)
                        }

                        myRef5 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId)
                                .child("buildings").child(userBuildingId).child("notificationsRead").child(mAuth.uid!!)

                        listenerRead = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                notificationsRead = dataSnapshot

                                notifyEventListeners(MyEvent(this, Type.UpdateUI_2))
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        }

                        myRef5!!.addValueEventListener(listenerRead!!)
                    }

                    Type.UpdateUI -> {
                        deleteMessagesAndUpdateUI()
                        System.gc()
                    }

                    Type.UpdateUI_2 -> {
                        deleteNotificationsAndUpdateUI()
                        System.gc()
                    }
                }
            }
        })
    }

    private fun deleteMessagesAndUpdateUI() {
        deleteMessages()

        if (messageSnapshot != null) {

            val notificationIntent = Intent(this, MessageFakeActivity::class.java)
            val contentIntent = PendingIntent.getActivity(this,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT)

            for (n in messageSnapshot!!.children) {
                if (Integer.parseInt(n.child("income").value!!.toString()) == 1 && Integer.parseInt(n.child("read").value!!.toString()) == 0) {
                    buildAndSendNotification("Сообщение от УК:", n.child("Text").value!!.toString(), "1", contentIntent, true)
                }
            }
        }
    }

    private fun deleteNotificationsAndUpdateUI() {
        deleteNotifications()

        if (notificationsSnapshot != null) {
            val notificationIntent = Intent(this, NotificationsFakeActivity::class.java)
            val contentIntent = PendingIntent.getActivity(this,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT)

            for (n in notificationsSnapshot!!.children) {
                if (notificationsRead.child(n.key!!).value == null) {
                    buildAndSendNotification("Уведомление для вашего дома:", n.child("text").value!!.toString(), "2", contentIntent, false)
                }
            }
        }
    }

    private fun deleteMessages() {
        val channel = notificationManager.getNotificationChannel("1")
        notificationManager.deleteNotificationChannel("1")
        notificationManager.createNotificationChannel(channel)
        notificationId = notificationManager.activeNotifications.size
    }

    private fun deleteNotifications() {
        val channel = notificationManager.getNotificationChannel("2")
        notificationManager.deleteNotificationChannel("2")
        notificationManager.createNotificationChannel(channel)
        notificationId = notificationManager.activeNotifications.size
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    private fun buildAndSendNotification(title: String, message: String, channelId: String, contentIntent: PendingIntent, mess: Boolean) {
        val builder: NotificationCompat.Builder = if (mess) {
            NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_menu_message)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
        } else {
            NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_menu_notif)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
        }

        notificationManager.notify(notificationId, builder.build())
        notificationId++
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener!!)
        }

        eventListeners!!.clear()
        if (myRef2 != null) {
            myRef2!!.removeEventListener(listenerBuilding!!)
        }
        if (myRef3 != null) {
            myRef3!!.removeEventListener(listenerMessages!!)
        }
        if (myRef4 != null) {
            myRef4!!.removeEventListener(listenerNotifications!!)
        }
        if (myRef5 != null) {
            myRef5!!.removeEventListener(listenerRead!!)
        }
        listenerBuilding = null
        listenerMessages = null
        listenerNotifications = null
        listenerRead = null
    }

    private fun deleteNotification() {
        val r = Thread(WaitAndDeleteNotificationChannel())
        r.start()
    }

    private inner class WaitAndDeleteNotificationChannel : Runnable {
        override fun run() {
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            notificationManager.deleteNotificationChannel("0")
        }
    }

    companion object {

        private var eventListeners: MutableList<MyEventListener>? = null

        fun notifyEventListeners(event: MyEvent) {
            for (eventListener in eventListeners!!) {
                eventListener.processEvent(event)
            }
        }
    }
}