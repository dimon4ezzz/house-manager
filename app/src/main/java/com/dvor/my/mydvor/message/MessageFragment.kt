package com.dvor.my.mydvor.message

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dvor.my.mydvor.*
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.data.Message
import com.dvor.my.mydvor.data.MessageBD
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class MessageFragment : Fragment(), View.OnClickListener {

    private lateinit var mAuth: FirebaseAuth
    internal var userStreetId: String = ""
    internal var organizationId: String = ""
    internal var userBuildingId: String = ""
    internal var myRef2: DatabaseReference? = null
    internal var myRef3: DatabaseReference? = null
    internal var messageSnapshot: DataSnapshot? = null
    internal var listenerBuilding: ValueEventListener? = null
    internal var listenerMessages: ValueEventListener? = null

    private val messages = ArrayList<Message>()
    private var messageText: TextView? = null
    private lateinit var messageList: ListView
    internal lateinit var context: Context

    private fun addEventListener(eventListener: MyEventListener) {
        eventListeners!!.add(eventListener)
    }

    fun notifyEventListeners(event: MyEvent) {
        for (eventListener in eventListeners!!) {
            eventListener.processEvent(event)
        }
    }

    //сохранение текста неотправленного сообщения
    //костыльно но работает
    override fun onSaveInstanceState(outState: Bundle) {
        if (messageText == null) {
            outState.putString("message", MainActivity.savedMessage)
        } else {
            outState.putString("message", messageText!!.text.toString())
            MainActivity.savedMessage = messageText!!.text.toString()
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

        val view = inflater.inflate(R.layout.fragment_message, container, false)
        // начальная инициализация списка
        // получаем элемент ListView
        messageList = view.findViewById(R.id.messageList)
        // создаем адаптер
        context = view.context

        mAuth = FirebaseAuth.getInstance()

        val myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.uid!!)

        val button = view.findViewById<Button>(R.id.send_message)
        button.setOnClickListener(this)

        messageText = view.findViewById(R.id.message_text)
        messageText!!.text = MainActivity.savedMessage
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

                                notifyEventListeners(MyEvent(this, Type.UpdateNews))
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        }

                        myRef3!!.addValueEventListener(listenerMessages!!)
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


    override fun onClick(v: View) {
        messageText!!.clearFocus()
        //скрыть клавиатуру
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(messageText!!.windowToken, 0)

        if (v.id == R.id.send_message) {
            sendMessage()
        }
    }

    private fun sendMessage() {
        if (messageText!!.text.toString().trim { it <= ' ' }.isEmpty())
            return
        val mes = MessageBD(0, messageText!!.text.toString(), Date().toString())
        val messageId = messages.size + 1
        messageText!!.text = ""
        MainActivity.savedMessage = ""
        FirebaseDatabase.getInstance().getReference("organization")
                .child(organizationId).child("messages")
                .child(mAuth.uid!!).child(messageId.toString()).setValue(mes)
    }

    private fun updateUI() {
        messages.clear()
        var userName: String

        if (messageSnapshot != null) {
            for (n in messageSnapshot!!.children) {
                if (Integer.parseInt(n.child("income").value!!.toString()) == 0)
                    userName = "Вы:"
                else {
                    userName = "Управляющая компания:"
                    FirebaseDatabase.getInstance().getReference("organization")
                            .child(organizationId).child("messages")
                            .child(mAuth.uid!!).child(n.key!!).child("read").setValue(1)
                }
                messages.add(Message(userName, n.child("text").value.toString(), n.child("date").value.toString()))
            }
        }
        messages.reverse()
        val messageAdapter = MessageAdapter(activity, R.layout.list_message, messages)
        // устанавливаем адаптер
        messageList.adapter = messageAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        eventListeners!!.clear()
        if (myRef2 != null) {
            myRef2!!.removeEventListener(listenerBuilding!!)
        }
        if (myRef3 != null) {
            myRef3!!.removeEventListener(listenerMessages!!)
        }
        listenerBuilding = null
        listenerMessages = null
    }

    companion object {

        private var eventListeners: MutableList<MyEventListener>? = null
    }
}