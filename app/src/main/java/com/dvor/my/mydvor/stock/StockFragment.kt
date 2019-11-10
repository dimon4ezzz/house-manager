package com.dvor.my.mydvor.stock

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView

import com.dvor.my.mydvor.MyEvent
import com.dvor.my.mydvor.MyEventListener
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.Type
import com.dvor.my.mydvor.data.Stock
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.util.ArrayList
import java.util.LinkedList

class StockFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null
    internal var userStreetId: String = ""
    internal lateinit var retailersStreet: DataSnapshot
    internal var myRef2: DatabaseReference? = null
    internal var myRef3: DatabaseReference? = null
    internal var retailers: MutableList<DataSnapshot> = LinkedList()
    internal var shopsID: MutableList<DataSnapshot> = LinkedList()
    internal var listenerRetailersStreet: ValueEventListener? = null
    internal var listenerRetailers: ValueEventListener? = null
    internal var itemListener: AdapterView.OnItemClickListener? = null

    private val stocks = ArrayList<Stock>()
    internal lateinit var stockList: ListView

    fun addEventListener(eventListener: MyEventListener) {
        eventListeners!!.add(eventListener)
    }

    fun notifyEventListeners(event: MyEvent) {
        for (eventListener in eventListeners!!) {
            eventListener.processEvent(event)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        if (eventListeners == null) {
            eventListeners = LinkedList()
        } else {
            eventListeners!!.clear()
        }

        val view = inflater.inflate(R.layout.fragment_stock, container, false)
        // начальная инициализация списка
        //setInitialData();
        // получаем элемент ListView
        stockList = view.findViewById(R.id.stocksList)
        // создаем адаптер
        val context = view.context

        mAuth = FirebaseAuth.getInstance()

        val myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth!!.uid!!)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    userStreetId = dataSnapshot.child("street_id").value!!.toString()

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
                            myRef2!!.removeEventListener(listenerRetailersStreet!!)
                        }

                        myRef2 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId).child("retailers")

                        listenerRetailersStreet = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                retailersStreet = dataSnapshot

                                notifyEventListeners(MyEvent(this, Type.UpdateRetailersStreet))
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        }

                        myRef2!!.addValueEventListener(listenerRetailersStreet!!)
                    }

                    Type.UpdateRetailersStreet -> {

                        if (myRef3 != null) {
                            myRef3!!.removeEventListener(listenerRetailers!!)
                        }

                        myRef3 = FirebaseDatabase.getInstance().getReference("retailers")

                        listenerRetailers = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {

                                retailers.clear()
                                shopsID.clear()

                                if (dataSnapshot.value != null) {
                                    for (retailerStreet in retailersStreet.children) {
                                        shopsID.add(retailerStreet.child("shops"))

                                        val retailerID = retailerStreet.child("id").value!!.toString()
                                        retailers.add(dataSnapshot.child(retailerID))

                                        notifyEventListeners(MyEvent(this, Type.UpdateRetailers))
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        }

                        myRef3!!.addValueEventListener(listenerRetailers!!)
                    }

                    Type.UpdateRetailers -> {
                        updateUI()
                        System.gc()
                    }
                }
            }
        })
        return view
    }

    private fun updateUI() {
        stocks.clear()

        for (i in retailers.indices) {
            //String retailerName = retailers.get(i).child("name").getValue().toString();

            val sales = retailers[i].child("sales")

            for (sale in sales.children) {
                stocks.add(Stock(sale.child("title").value!!.toString(),
                        sale.child("text").value!!.toString(),
                        sale.child("address").value!!.toString(), sale.child("img").value!!.toString()))
            }
        }

        val newsAdapter = StockAdapter(activity, R.layout.list_stocks, stocks)
        // устанавливаем адаптер
        stockList.adapter = newsAdapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        eventListeners!!.clear()
        if (myRef2 != null) {
            myRef2!!.removeEventListener(listenerRetailersStreet!!)
        }
        if (myRef3 != null) {
            myRef3!!.removeEventListener(listenerRetailers!!)
        }
        listenerRetailersStreet = null
        listenerRetailers = null
    }

    companion object {

        private var eventListeners: MutableList<MyEventListener>? = null
    }
}