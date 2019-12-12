package com.dvor.my.mydvor.emergency


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.data.EmergencyContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_emergency.*

/**
 * A simple [Fragment] subclass.
 */
class EmergencyFragment : Fragment() {

    private var mAuth = FirebaseAuth.getInstance()

    private var usersBranch = FirebaseDatabase.getInstance().getReference("users")
    private var streetsBranch = FirebaseDatabase.getInstance().getReference("streets")
    private var organizationBranch = FirebaseDatabase.getInstance().getReference("organization")

    private var usersBranchListener: ValueEventListener? = null
    private var streetsBranchListener: ValueEventListener? = null
    private var organizationsBranchListener: ValueEventListener? = null

    private lateinit var listOnView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_emergency, container, false)
        listOnView = view.findViewById(R.id.emergency_list)
        listOnView.layoutManager = LinearLayoutManager(view.context)
        listOnView.setHasFixedSize(true)
        listenUsersBranch()
        return view
    }

    private fun listenUsersBranch() {
        usersBranchListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("state", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                listenStreetsBranch(
                        p0.child(mAuth.currentUser!!.uid)
                                .child("street_id")
                                .value.toString(),
                        p0.child(mAuth.currentUser!!.uid)
                                .child("building_id")
                                .value.toString()
                )
            }
        }

        usersBranch.addValueEventListener(usersBranchListener!!)
    }

    private fun listenStreetsBranch(streetId: String, buildingId: String) {
        streetsBranchListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("state", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                listenOrganizationsBranch(
                        p0.child(streetId)
                                .child("buildings")
                                .child(buildingId)
                                .child("organization_id")
                                .value.toString()
                )
            }
        }

        streetsBranch.addValueEventListener(streetsBranchListener!!)
    }

    private fun listenOrganizationsBranch(id: String) {
        organizationsBranchListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("state", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                updateUI(
                        p0.child(id)
                                .child("emergencies")
                                .children.map {
                            EmergencyContact(
                                    tel = it.child("tel").value.toString(),
                                    telName = it.child("name").value.toString()
                            )
                        }
                )
            }
        }

        organizationBranch.addValueEventListener(organizationsBranchListener!!)
    }

    private fun updateUI(list: List<EmergencyContact>) {
        emergency_list.adapter = EmergencyAdapter(list)
    }

    override fun onDestroy() {
        super.onDestroy()
        usersBranchListener?.let {
            usersBranch.removeEventListener(it)
        }

        streetsBranchListener?.let {
            streetsBranch.removeEventListener(it)
        }

        organizationsBranchListener?.let {
            organizationBranch.removeEventListener(it)
        }

        usersBranchListener = null
        streetsBranchListener = null
        organizationsBranchListener = null
    }
}
