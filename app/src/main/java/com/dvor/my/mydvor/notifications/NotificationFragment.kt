package com.dvor.my.mydvor.notifications


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.dvor.my.mydvor.MyEvent
import com.dvor.my.mydvor.MyEventListener
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.Type
import com.dvor.my.mydvor.data.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class NotificationFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    internal var userStreetId: String = ""
    internal var userBuildingId: String = ""
    internal var myRef2: DatabaseReference? = null
    internal var notificationSnapshot: DataSnapshot? = null
    internal var listenerBuilding: ValueEventListener? = null
    private var listenerMessages: ValueEventListener? = null

    private val notifications = ArrayList<Notification>()

    private lateinit var notificationList: ListView
    internal lateinit var context: Context

    private fun addEventListener(eventListener: MyEventListener) {
        eventListeners!!.add(eventListener)
    }

    fun notifyEventListeners(event: MyEvent) {
        for (eventListener in eventListeners!!) {
            eventListener.processEvent(event)
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreate(savedInstanceState)

        if (eventListeners == null) {
            eventListeners = LinkedList()
        } else {
            eventListeners!!.clear()
        }

        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        // начальная инициализация списка
        // получаем элемент ListView
        notificationList = view.findViewById(R.id.notificationList)
        // создаем адаптер
        context = view.context

        mAuth = FirebaseAuth.getInstance()

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
                if (event.source == null || event.type == null) {
                    return
                }

                when (event.type) {
                    Type.UpdateAddressID -> {

                        if (myRef2 != null) {
                            myRef2!!.removeEventListener(listenerBuilding!!)
                        }

                        myRef2 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId)
                                .child("buildings").child(userBuildingId).child("notifications")

                        listenerBuilding = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                notificationSnapshot = dataSnapshot

                                notifyEventListeners(MyEvent(this, Type.UpdateNews))
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        }

                        myRef2!!.addValueEventListener(listenerBuilding!!)
                    }

                    Type.UpdateNews -> {
                        updateUI()
                        System.gc()
                    }
                }
            }
        })

        return view
    }


    private fun updateUI() {
        notifications.clear()
        val userName: String

        if (notificationSnapshot != null) {
            for (n in notificationSnapshot!!.children) {
                notifications.add(Notification(n.child("text").value!!.toString(), n.child("date").value!!.toString()))
                FirebaseDatabase.getInstance().getReference("streets")
                        .child(userStreetId).child("buildings")
                        .child(userBuildingId).child("notificationsRead").child(mAuth.uid!!).child(n.key!!).setValue(1)
            }
        }
        notifications.reverse()
        val notificationAdapter = NotificationAdapter(activity, R.layout.list_notification, notifications)
        // устанавливаем адаптер
        notificationList.adapter = notificationAdapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        eventListeners!!.clear()
        if (myRef2 != null) {
            myRef2!!.removeEventListener(listenerBuilding!!)
        }
        listenerBuilding = null
        listenerMessages = null
    }

    companion object {

        private var eventListeners: MutableList<MyEventListener>? = null
    }
}