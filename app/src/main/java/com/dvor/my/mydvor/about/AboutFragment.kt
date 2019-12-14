package com.dvor.my.mydvor.about


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dvor.my.mydvor.R

/**
 * A simple [Fragment] subclass.
 */
class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_about, container, false)

        view.findViewById<TextView>(R.id.app_version).text = resources.getString(
                R.string.version,
                context!!.packageManager.getPackageInfo("com.dvor.my.mydvor", 0).versionName
        )

        return view
    }


}
