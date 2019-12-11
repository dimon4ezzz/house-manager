package com.dvor.my.mydvor.stock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.data.Retailer
import com.dvor.my.mydvor.data.Stock
import com.dvor.my.mydvor.firebase.RetailersBranchDao
import com.dvor.my.mydvor.firebase.UsersBranchDao
import java.util.*

class StockFragment : Fragment() {
    private lateinit var stockList: ListView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_stock, container, false)
        stockList = view.findViewById(R.id.stocksList)

        UsersBranchDao.listenUsersBranch { user ->
            RetailersBranchDao.listenRetailersBranch(user.street_id) { list ->
                updateUI(list)
            }
        }

        return view
    }

    private fun updateUI(retailers: List<Retailer>) {
        val stocks = ArrayList<Stock>()
        for (ret in retailers) {
            stocks.addAll(ret.stocks!!.asIterable())
        }

        val newsAdapter = StockAdapter(activity, R.layout.list_stocks, stocks)
        stockList.adapter = newsAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RetailersBranchDao.stopListenRetailersBranch()
    }
}