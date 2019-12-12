package com.dvor.my.mydvor.menu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuFragment : Fragment() {

    private val mAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    lateinit var user: User

    private var userBranch = database.child("users")
    private var streetBranch = database.child("streets")

    private lateinit var userListener: ValueEventListener
    private lateinit var streetListener: ValueEventListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        userBranch = userBranch.child(mAuth.uid.toString())
        // variable init
        user = User("", "", "", "", "", "", "")
        setUserListener()

        view.findViewById<Button>(R.id.ib_emergency).setOnClickListener { view.findNavController().navigate(emergencyFragment) }
        view.findViewById<Button>(R.id.ib_news).setOnClickListener { view.findNavController().navigate(newsFragmentId) }
        view.findViewById<Button>(R.id.ib_stock).setOnClickListener { view.findNavController().navigate(stockFragmentId) }
        view.findViewById<Button>(R.id.ib_message).setOnClickListener { view.findNavController().navigate(messageFragmentId) }
        view.findViewById<Button>(R.id.ib_notification).setOnClickListener { view.findNavController().navigate(notificationFragmentId) }
        view.findViewById<Button>(R.id.ib_service).setOnClickListener { view.findNavController().navigate(serviceFragmentId) }
        view.findViewById<Button>(R.id.ib_settings).setOnClickListener { view.findNavController().navigate(settingsFragmentId) }

        view.findViewById<Button>(R.id.ib_logout).setOnClickListener {
            mAuth.signOut()
            context!!.dataDir.deleteRecursively()
        }

        return view
    }

    /**
     * Updates `username` textView
     */
    private fun updateUsername() {
        view?.findViewById<TextView>(R.id.username)?.text = getUsername()
    }

    /**
     * Updates `address` textView
     */
    private fun updateAddress() {
        view?.findViewById<TextView>(R.id.address)?.text = getAddress()
    }

    /**
     * Returns name and surname
     */
    private fun getUsername(): String = "${user.name} ${user.surname}"

    /**
     * Returns address, where user live
     */
    private fun getAddress(): String = "${user.street} ${user.building} кв. ${user.apartment}"

    /**
     * Sets database listener for fetch data from `users` branch.
     *
     * Other branches are not implemented
     */
    private fun setUserListener() {
        userListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // судить можно по любому, в том числе и по `name`
                if (dataSnapshot.child("name").value == null) {
                    view!!.findViewById<ConstraintLayout>(R.id.user_info).visibility = View.GONE
                } else {
                    view!!.findViewById<ConstraintLayout>(R.id.user_info).visibility = View.VISIBLE
                }

                user.name = dataSnapshot.child("name").value.toString()
                user.surname = dataSnapshot.child("surname").value.toString()
                user.street_id = dataSnapshot.child("street_id").value.toString()
                user.building_id = dataSnapshot.child("building_id").value.toString()
                user.apartment = dataSnapshot.child("apartment").value.toString()

                setAddressListener()

                updateUsername()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("state", databaseError.message)
            }
        }

        userBranch.addValueEventListener(userListener)
    }

    /**
     * Sets database listener for fetch data from `streets` branch
     */
    private fun setAddressListener() {
        streetListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                user.street = dataSnapshot.child("name").value.toString()
                user.building = dataSnapshot
                        .child("buildings")
                        .child(user.building_id)
                        .child("number").value.toString()

                updateAddress()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("state", databaseError.message)
            }
        }

        streetBranch = streetBranch.child(user.street_id)
        streetBranch.addValueEventListener(streetListener)
    }

    override fun onStop() {
        super.onStop()

        userBranch.removeEventListener(userListener)
        streetBranch.removeEventListener(streetListener)

        userBranch = database.child("users")
        streetBranch = database.child("streets")
    }

    companion object {
        const val emergencyFragment: Int = R.id.emergencyFragment
        const val stockFragmentId: Int = R.id.stockFragment
        const val messageFragmentId: Int = R.id.messageFragment
        const val notificationFragmentId: Int = R.id.notificationFragment

        const val newsFragmentId: Int = R.id.newsFragment
        const val serviceFragmentId: Int = R.id.serviceFragment
        const val settingsFragmentId: Int = R.id.settingsFragment
    }
}
