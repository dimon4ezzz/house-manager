package com.dvor.my.mydvor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ServiceAdapter extends ArrayAdapter<Service> {

    private LayoutInflater inflater;
    private int layout;
    private List<Service> services;
    private Context context;

    public ServiceAdapter(Context context, int resource, List<Service> services) {
        super(context, resource, services);
        this.services = services;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
        this.context=context;

    }
    public View getView(int position, View convertView, ViewGroup parent) {

        View view=inflater.inflate(this.layout, parent, false);

        ImageView imgView = (ImageView) view.findViewById(R.id.image);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView textView = (TextView) view.findViewById(R.id.text);
        final TextView phone =(TextView) view.findViewById(R.id.phone);

        Service service = services.get(position);

       // imgView.setImageResource(service.getImgResource());
        titleView.setText(service.getTitle());
        textView.setText(service.getText());
        phone.setText(service.getPhone());

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone.getText().toString(), null));
                context.startActivity(intent);

            }
        });

        return view;
    }
}