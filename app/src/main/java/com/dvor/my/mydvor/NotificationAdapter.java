package com.dvor.my.mydvor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;


import java.util.List;

public class NotificationAdapter extends ArrayAdapter<Notification> {

    private LayoutInflater inflater;
    private int layout;
    private List<Notification>notifications;
private Context context;
    private String text;
    public NotificationAdapter(Context context, int resource, List<Notification> notifications) {
        super(context, resource, notifications);
        this.notifications = notifications;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
        this.context=context;
    }
    public View getView(int position, View convertView, ViewGroup parent) {

        View view=inflater.inflate(this.layout, parent, false);

        TextView textView = (TextView) view.findViewById(R.id.text);
        TextView dataView = (TextView) view.findViewById(R.id.data);
        Notification notification = notifications.get(position);
        text=notification.getText();
        ImageButton shareButton = (ImageButton) view.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent,"Поделиться"));

            }
        });

        textView.setText(text);
        dataView.setText(DataConverter.convert(notification.getData()));
        return view;
    }


}