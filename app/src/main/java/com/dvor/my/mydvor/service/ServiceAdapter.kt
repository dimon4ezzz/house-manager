package com.dvor.my.mydvor.service

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.data.Service

class ServiceAdapter(private val context: FragmentActivity?, private val layout: Int, private val services: List<Service>) : ArrayAdapter<Service>(context!!, layout, services) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = inflater.inflate(this.layout, parent, false)

        val imgView = view.findViewById<ImageView>(R.id.image)
        val titleView = view.findViewById<TextView>(R.id.title)
        val textView = view.findViewById<TextView>(R.id.text)
        val phone = view.findViewById<TextView>(R.id.phone)

        val service = services[position]

        // imgView.setImageResource(service.getImgResource());
        titleView.text = service.title
        textView.text = service.text
        phone.text = service.phone

        phone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone.text.toString(), null))
            context?.startActivity(intent)
        }

        return view
    }
}