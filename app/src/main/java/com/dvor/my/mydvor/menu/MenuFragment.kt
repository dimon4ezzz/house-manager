package com.dvor.my.mydvor.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.dvor.my.mydvor.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MenuFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        val mAuth = FirebaseAuth.getInstance()

        view.findViewById<Button>(R.id.ib_news).setOnClickListener { goto(newsFragmentId) }
        view.findViewById<Button>(R.id.ib_stock).setOnClickListener { goto(stockFragmentId) }
        view.findViewById<Button>(R.id.ib_message).setOnClickListener { goto(messageFragmentId) }
        view.findViewById<Button>(R.id.ib_notification).setOnClickListener { goto(notificationFragmentId) }
//        view.findViewById<Button>(R.id.ib_service).setOnClickListener { goto(serviceFragmentId) }

        view.findViewById<Button>(R.id.ib_logout).setOnClickListener { mAuth.signOut() }

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

    companion object {
        const val newsFragmentId: Int = R.id.newsFragment
        const val stockFragmentId: Int = R.id.stockFragment
        const val messageFragmentId: Int = R.id.messageFragment
        const val notificationFragmentId: Int = R.id.notificationFragment
        const val serviceFragmentId: Int = R.id.serviceFragment
        // TODO settings fragment
    }
}
