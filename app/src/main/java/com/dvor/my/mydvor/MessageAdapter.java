package com.dvor.my.mydvor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {

    private LayoutInflater inflater;
    private int layout;
    private List<Message> messages;

    public MessageAdapter(Context context, int resource, List<Message> messages) {
        super(context, resource, messages);
        this.messages = messages;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }
    public View getView(int position, View convertView, ViewGroup parent) {

        View view=inflater.inflate(this.layout, parent, false);


        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView textView = (TextView) view.findViewById(R.id.text);
        TextView dataView = (TextView) view.findViewById(R.id.data);
        Message message = messages.get(position);

        titleView.setText(message.getTitle());
        textView.setText(message.getText());
        dataView.setText(DataConverter.convert(message.getData()));
        return view;
    }
}