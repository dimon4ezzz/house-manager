package com.dvor.my.mydvor.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.dvor.my.mydvor.R
import com.google.firebase.auth.FirebaseAuth

class MenuFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        val mAuth = FirebaseAuth.getInstance()

        view.findViewById<Button>(R.id.ib_logout).setOnClickListener { mAuth.signOut() }

        return view
    }

    // TODO реализовать переход по фрагментам
    /*fun goto(nextFragment: Fragment) {
        activity!!.supportFragmentManager
                .beginTransaction()
                .replace(R.id.menuFragment, nextFragment, "test")
                .addToBackStack(null)
                .commit()
    }*/
}
