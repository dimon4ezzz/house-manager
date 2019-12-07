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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MenuFragment : Fragment() {

    lateinit var mAuth: FirebaseAuth
    lateinit var database: DatabaseReference
    lateinit var user: User

    lateinit var userBranch: DatabaseReference
    lateinit var streetBranch: DatabaseReference

    lateinit var userListener: ValueEventListener
    lateinit var streetListener: ValueEventListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        // variable init
        user = User("", "", "", "", "", "", "")
        setUserListener()

        view.findViewById<Button>(R.id.ib_news).setOnClickListener { goto(newsFragmentId) }
        view.findViewById<Button>(R.id.ib_stock).setOnClickListener { goto(stockFragmentId) }
        view.findViewById<Button>(R.id.ib_message).setOnClickListener { goto(messageFragmentId) }
        view.findViewById<Button>(R.id.ib_notification).setOnClickListener { goto(notificationFragmentId) }
        view.findViewById<Button>(R.id.ib_service).setOnClickListener {
            val action = MenuFragmentDirections.actionMenuFragmentToServiceFragment()
            view.findNavController().navigate(action)
        }

        view.findViewById<Button>(R.id.ib_logout).setOnClickListener {
            mAuth.signOut()
            context!!.dataDir.deleteRecursively()
        }

        return view
    }

    /**
     * Implements moving between fragments,
     * which are on bottom bar.
     *
     * There is no jump to `service` or `settings` fragments
     */
    private fun goto(fragment: Int) {
        activity!!.findViewById<BottomNavigationView>(R.id.nav_view)
                .selectedItemId = fragment

        // Это не работает, так как в этом проекте другой навигатор
        // и вызывать нужно его (см. выше)
        /*activity!!.supportFragmentManager
                .beginTransaction()
                .replace(R.id.menuFragment, nextFragment) // nextFragment из аргумента функции
                .addToBackStack(null)
                .commit()*/
    }

    /**
     * Updates view, set `username` and `address` textviews
     */
    private fun updateUsername() {
        view?.findViewById<TextView>(R.id.username)?.text = getUsername()
    }

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

        userBranch = database.child("users").child(mAuth.uid.toString())
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
                        .child(user.building_id.toString())
                        .child("number").value.toString()

                updateAddress()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("state", databaseError.message)
            }
        }

        streetBranch = database.child("streets").child(user.street_id.toString())
        streetBranch.addValueEventListener(streetListener)
    }

    override fun onStop() {
        super.onStop()

        userBranch.removeEventListener(userListener)
        streetBranch.removeEventListener(streetListener)
    }

    companion object {
        const val newsFragmentId: Int = R.id.newsFragment
        const val stockFragmentId: Int = R.id.stockFragment
        const val messageFragmentId: Int = R.id.messageFragment
        const val notificationFragmentId: Int = R.id.notificationFragment
        const val serviceFragmentId: Int = R.id.serviceFragment
        // TODO settings fragment
    }
}
