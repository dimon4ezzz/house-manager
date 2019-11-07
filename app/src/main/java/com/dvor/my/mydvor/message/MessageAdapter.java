package com.dvor.my.mydvor.message;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dvor.my.mydvor.utils.DataConverter;
import com.dvor.my.mydvor.R;
import com.dvor.my.mydvor.data.Message;

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


        TextView titleView = view.findViewById(R.id.title);
        TextView textView = view.findViewById(R.id.text);
        TextView dataView = view.findViewById(R.id.data);
        Message message = messages.get(position);

        titleView.setText(message.getTitle());
        textView.setText(message.getText());
        dataView.setText(DataConverter.convert(message.getData()));
        return view;
    }
}