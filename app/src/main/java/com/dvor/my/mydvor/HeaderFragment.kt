package com.dvor.my.mydvor

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HeaderFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null
    private var userName: TextView? = null
    private var userAddress: TextView? = null
    internal var userStreetId: String = ""
    internal var userBuildingId: String = ""
    internal var userApartment: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.nav_header_menu, container, false)
        userName = view.findViewById(R.id.user_name)
        userAddress = view.findViewById(R.id.user_address)

        mAuth = FirebaseAuth.getInstance()

        val myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth!!.uid!!)

        myRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    userName!!.text = dataSnapshot.child("name").value!!.toString() + " " + dataSnapshot.child("surname").value!!.toString()
                    userStreetId = dataSnapshot.child("street_id").value!!.toString()
                    userBuildingId = dataSnapshot.child("building_id").value!!.toString()
                    userApartment = dataSnapshot.child("apartment").value!!.toString()

                    val myRef2 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId)

                    myRef2.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.value != null) {
                                val address = dataSnapshot.child("street").value!!.toString() + ", д. " + dataSnapshot.child("buildings").child(userBuildingId).child("number").value!!.toString() + ", кв. " + userApartment
                                userAddress!!.text = address
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        return view
    }
}