package com.dvor.my.mydvor.stock

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
import com.dvor.my.mydvor.Storage
import com.dvor.my.mydvor.data.Stock

class StockAdapter(internal var context: FragmentActivity?, private val layout: Int, private val stocks: List<Stock>) : ArrayAdapter<Stock>(context!!, layout, stocks) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = inflater.inflate(this.layout, parent, false)

        val imgView = view.findViewById<ImageView>(R.id.image)
        val titleView = view.findViewById<TextView>(R.id.title)
        val textView = view.findViewById<TextView>(R.id.text)
        val addressView = view.findViewById<TextView>(R.id.adress)

        val stock = stocks[position]

        titleView.text = stock.title
        textView.text = stock.text
        addressView.text = stock.address
        Storage.downloadPicture(stock.imgResource.toString(), imgView)

        addressView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:56.8138122,60.5145084")
            }
            if (intent.resolveActivity(context!!.packageManager) != null) {
                context!!.startActivity(intent)
            }
        }

        return view
    }
}