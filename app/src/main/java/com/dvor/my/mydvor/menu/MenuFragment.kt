package com.dvor.my.mydvor.menu

import android.os.Bundle
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
import com.dvor.my.mydvor.firebase.AddressDelegator
import com.dvor.my.mydvor.firebase.Auth
import com.dvor.my.mydvor.firebase.UsersBranchDao

class MenuFragment : Fragment() {

    lateinit var user: User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
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
            Auth.signOut()
            context!!.dataDir.deleteRecursively()
        }

        return view
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
        try {
            UsersBranchDao.listenUsersBranch { user ->
                this.user = user

                setAddressListener()

                updateUsername()

                view!!.findViewById<ConstraintLayout>(R.id.user_info).visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            view!!.findViewById<ConstraintLayout>(R.id.user_info).visibility = View.GONE
        }
    }

    /**
     * Sets database listener for fetch data from `streets` branch
     */
    private fun setAddressListener() {
        AddressDelegator.listenAddress(user) { user ->
            this.user = user

            updateAddress()
        }
    }

    override fun onStop() {
        super.onStop()

        UsersBranchDao.stopListenUsersBranch()
        AddressDelegator.stopListenAddress()
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
