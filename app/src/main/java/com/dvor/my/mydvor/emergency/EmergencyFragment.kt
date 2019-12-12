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
    private var organizationBranchListener: ValueEventListener? = null

    private lateinit var listOnView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_emergency, container, false)
        listOnView = view.findViewById(R.id.emergency_list)
        listOnView.layoutManager = LinearLayoutManager(view.context)
        listOnView.setHasFixedSize(true)
        listenUsersBranch()
        return view
    }

    private fun listenUsersBranch() {
        usersBranch = usersBranch.child(mAuth.currentUser!!.uid)

        usersBranchListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("state", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                listenStreetsBranch(
                        p0.child("street_id")
                                .value.toString(),
                        p0.child("building_id")
                                .value.toString()
                )
            }
        }

        usersBranch.addValueEventListener(usersBranchListener!!)
    }

    private fun listenStreetsBranch(streetId: String, buildingId: String) {
        streetsBranch = streetsBranch.child(streetId)
                .child("buildings")
                .child(buildingId)
                .child("organization_id")

        streetsBranchListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("state", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                listenOrganizationsBranch(
                        p0.value.toString()
                )
            }
        }

        streetsBranch.addValueEventListener(streetsBranchListener!!)
    }

    private fun listenOrganizationsBranch(id: String) {
        organizationBranch = organizationBranch.child(id)
                .child("emergencies")

        organizationBranchListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("state", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                updateUI(
                        p0.children.map {
                            EmergencyContact(
                                    tel = it.child("tel").value.toString(),
                                    telName = it.child("name").value.toString()
                            )
                        }
                )
            }
        }

        organizationBranch.addValueEventListener(organizationBranchListener!!)
    }

    private fun updateUI(list: List<EmergencyContact>) {
        emergency_list.adapter = EmergencyAdapter(list)
    }

    /**
     * При скрытии фрагмента просто обнулить все листенеры и использовать дефолтные пути для веток.
     */
    override fun onPause() {
        super.onPause()
        usersBranchListener?.let {
            usersBranch.removeEventListener(it)
        }

        streetsBranchListener?.let {
            streetsBranch.removeEventListener(it)
        }

        organizationBranchListener?.let {
            organizationBranch.removeEventListener(it)
        }

        usersBranch = FirebaseDatabase.getInstance().getReference("users")
        streetsBranch = FirebaseDatabase.getInstance().getReference("streets")
        organizationBranch = FirebaseDatabase.getInstance().getReference("organization")

        usersBranchListener = null
        streetsBranchListener = null
        organizationBranchListener = null
    }
}
