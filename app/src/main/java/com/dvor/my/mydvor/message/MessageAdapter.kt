package com.dvor.my.mydvor.message

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.data.Message
import com.dvor.my.mydvor.utils.DateConverter

class MessageAdapter(context: FragmentActivity?, private val layout: Int, private val messages: List<Message>) : ArrayAdapter<Message>(context!!, layout, messages) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = inflater.inflate(this.layout, parent, false)

        val titleView = view.findViewById<TextView>(R.id.title)
        val textView = view.findViewById<TextView>(R.id.text)
        val dataView = view.findViewById<TextView>(R.id.data)
        val message = messages[position]

        titleView.text = message.title
        textView.text = message.text
        dataView.text = DateConverter.convert(message.date.toString())
        dataView.setOnClickListener {
            Toast.makeText(context, message.date.toString(), Toast.LENGTH_SHORT).show()
        }
        return view
    }
}