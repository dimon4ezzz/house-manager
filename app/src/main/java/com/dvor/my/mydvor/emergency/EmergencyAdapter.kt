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
        private val phone = itemView.findViewById<TextView>(R.id.tv_phone_number)
        private val phoneOwner = itemView.findViewById<TextView>(R.id.tv_phone_owner)
        private val phoneDepartment = itemView.findViewById<TextView>(R.id.tv_phone_department)

        fun bind(contact: EmergencyContact) {
            phone.text = contact.phone
            itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${contact.phone}")
                }
                if (intent.resolveActivity(itemView.context.packageManager) != null) {
                    itemView.context.startActivity(intent)
                }
            }

            // если прописан человек, показывать это
            contact.owner?.let {
                phoneOwner.text = it
                phoneOwner.visibility = View.VISIBLE
            }

            phoneDepartment.text = contact.department
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