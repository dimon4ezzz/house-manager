package com.dvor.my.mydvor.service

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.dvor.my.mydvor.MyEvent
import com.dvor.my.mydvor.MyEventListener
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.Type
import com.dvor.my.mydvor.data.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class ServiceFragment : Fragment() {
    internal var data = arrayOf("Доставка воды", "Продукты", "Сантехник")

    private var mAuth: FirebaseAuth? = null
    internal var userStreetId: String = ""
    internal var organizationId: String = ""
    internal var userBuildingId: String = ""
    internal var myRef2: DatabaseReference? = null
    internal var myRef3: DatabaseReference? = null
    internal var servicesSnapshot: DataSnapshot? = null
    internal var listenerBuilding: ValueEventListener? = null
    internal var listenerService: ValueEventListener? = null

    private val services = ArrayList<Service>()
    private lateinit var servicesList: ListView
    internal var serviceType: Long? = null
    internal lateinit var context: Context
    private lateinit var thisview: View

    private fun addEventListener(eventListener: MyEventListener) {
        eventListeners!!.add(eventListener)
    }

    fun notifyEventListeners(event: MyEvent) {
        for (eventListener in eventListeners!!) {
            eventListener.processEvent(event)
        }
    }

    private fun spinner() {
        val adapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, data)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner = thisview.findViewById<Spinner>(R.id.spinner)
        spinner.adapter = adapter
        spinner.prompt = "Title"
        spinner.setSelection(0)
        // обработчик нажатия
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?,
                                        position: Int, id: Long) {
                serviceType = id
                takeDataSnapshot()
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        if (eventListeners == null) {
            eventListeners = LinkedList()
        } else {
            eventListeners!!.clear()
        }

        thisview = inflater.inflate(R.layout.fragment_service, container, false)
        // начальная инициализация списка
        // получаем элемент ListView
        servicesList = thisview.findViewById(R.id.newsList)
        context = thisview.context
        spinner()
        takeDataSnapshot()
        return thisview
    }

    private fun takeDataSnapshot() {
        mAuth = FirebaseAuth.getInstance()
        val myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth!!.uid!!)
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    userStreetId = dataSnapshot.child("street_id").value!!.toString()
                    userBuildingId = dataSnapshot.child("building_id").value!!.toString()

                    notifyEventListeners(MyEvent(this, Type.UpdateAddressID))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
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

                        myRef2 = FirebaseDatabase.getInstance()
                                .getReference("streets")
                                .child(userStreetId)
                                .child("buildings")
                                .child(userBuildingId)
                                .child("organization_id")

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
                            myRef3!!.removeEventListener(listenerService!!)
                        }

                        myRef3 = FirebaseDatabase.getInstance()
                                .getReference("organization")
                                .child(organizationId)
                                .child("services")
                                .child((serviceType!!)
                                        .toString())

                        listenerService = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                servicesSnapshot = dataSnapshot

                                notifyEventListeners(MyEvent(this, Type.UpdateNews))
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        }

                        myRef3!!.addValueEventListener(listenerService!!)
                    }

                    Type.UpdateNews -> {
                        updateUI()
                        System.gc()
                    }
                }
            }
        })
    }

    private fun updateUI() {
        services.clear()

        if (servicesSnapshot != null) {
            for (n in servicesSnapshot!!.children) {
                services.add(Service(n.child("provider").value!!.toString(), n.child("text").value!!.toString(),
                        n.child("phone").value!!.toString()))
            }
        }

        val serviceAdapter = ServiceAdapter(activity, R.layout.list_service, services)
        // устанавливаем адаптер
        servicesList.adapter = serviceAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        eventListeners!!.clear()
        if (myRef2 != null) {
            myRef2!!.removeEventListener(listenerBuilding!!)
        }
        if (myRef3 != null) {
            myRef3!!.removeEventListener(listenerService!!)
        }
        listenerBuilding = null
        listenerService = null
    }

    companion object {

        private var eventListeners: MutableList<MyEventListener>? = null
    }
}

