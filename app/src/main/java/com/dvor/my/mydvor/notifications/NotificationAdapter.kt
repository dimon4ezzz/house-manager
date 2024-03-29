package com.dvor.my.mydvor.notifications


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.data.Notification
import com.dvor.my.mydvor.utils.DateConverter

class NotificationAdapter(private val context: FragmentActivity?, private val layout: Int, private val notifications: List<Notification>) : ArrayAdapter<Notification>(context!!, layout, notifications) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = inflater.inflate(this.layout, parent, false)

        val textView = view.findViewById<TextView>(R.id.text)
        val dataView = view.findViewById<TextView>(R.id.data)
        val notification = notifications[position]
        val text = notification.text
        val shareButton = view.findViewById<ImageButton>(R.id.shareButton)
        dataView.setOnClickListener {
            Toast.makeText(context, notification.date.toString(), Toast.LENGTH_SHORT).show()
        }
        shareButton.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, text)
            sendIntent.type = "text/plain"
            context?.startActivity(Intent.createChooser(sendIntent, "Поделиться"))
        }

        textView.text = text
        dataView.text = DateConverter.convert(notification.date.toString())
        return view
    }


}