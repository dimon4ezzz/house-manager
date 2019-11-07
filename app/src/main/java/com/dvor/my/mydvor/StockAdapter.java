package com.dvor.my.mydvor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class StockAdapter extends ArrayAdapter<Stock> {

    private LayoutInflater inflater;
    private int layout;
    private List<Stock> stocks;
    Context context;

    public StockAdapter(Context context, int resource, List<Stock> stocks) {
        super(context, resource, stocks);
        this.stocks = stocks;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
        this.context=context;
    }
    public View getView(int position, View convertView, ViewGroup parent) {

        View view=inflater.inflate(this.layout, parent, false);

        ImageView imgView = (ImageView) view.findViewById(R.id.image);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView textView = (TextView) view.findViewById(R.id.text);
        final TextView addressView=(TextView) view.findViewById(R.id.adress);

        final Stock stock = stocks.get(position);

        titleView.setText(stock.getTitle());
        textView.setText(stock.getText());
        addressView.setText(stock.getAddress());
        Storage.downloadPicture(stock.getImgResource(), imgView);

        addressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
try {
    Uri gmmIntentUri = Uri.parse("geo:56.8138122,60.5145084,11?q=" + Uri.encode(stock.getAddress()));
    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
    mapIntent.setPackage("com.google.android.apps.maps");
    if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
        context.startActivity(mapIntent);
    }
}
               catch (Exception ex)
               {
                   Log.d("state", ex.getMessage());
               }

            }
        });

        return view;
    }
}