package com.dvor.my.mydvor.emergency

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.data.EmergencyContact

class EmergencyAdapter(contacts: List<EmergencyContact>) : RecyclerView.Adapter<EmergencyAdapter.EmergencyViewHolder>() {
    class EmergencyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tel = itemView.findViewById<TextView>(R.id.tel)
        private val telName = itemView.findViewById<TextView>(R.id.tel_name)

        fun bind(contact: EmergencyContact) {
            tel.text = contact.tel
            tel.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${contact.tel}")
                }
                if (intent.resolveActivity(itemView.context.packageManager) != null) {
                    itemView.context.startActivity(intent)
                }
            }

            telName.text = contact.telName
        }
    }

    private var list: List<EmergencyContact> = contacts

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmergencyViewHolder {
        val context = parent.context
        val layoutId = R.layout.list_emergency
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(layoutId, parent, false)
        return EmergencyViewHolder(view)
    }

    override fun getItemCount(): Int =
            list.count()

    override fun onBindViewHolder(holder: EmergencyViewHolder, position: Int) =
            holder.bind(list[position])
}