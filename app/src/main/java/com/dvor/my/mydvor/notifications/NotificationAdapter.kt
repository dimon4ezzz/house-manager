package com.dvor.my.mydvor.notifications

import android.content.Intent
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView


import com.dvor.my.mydvor.utils.DataConverter
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.data.Notification

class NotificationAdapter(private val context: FragmentActivity?, private val layout: Int, private val notifications: List<Notification>) : ArrayAdapter<Notification>(context, layout, notifications) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var text: String? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = inflater.inflate(this.layout, parent, false)

        val textView = view.findViewById<TextView>(R.id.text)
        val dataView = view.findViewById<TextView>(R.id.data)
        val notification = notifications[position]
        text = notification.text
        val shareButton = view.findViewById<ImageButton>(R.id.shareButton)
        shareButton.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, text)
            sendIntent.type = "text/plain"
            context?.startActivity(Intent.createChooser(sendIntent, "Поделиться"))
        }

        textView.text = text
        dataView.text = DataConverter.convert(notification.data.toString())
        return view
    }


}