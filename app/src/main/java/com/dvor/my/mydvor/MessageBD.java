package com.dvor.my.mydvor;

public class MessageBD {

    public int income;
    public String Text;
    public String date;

    public MessageBD() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public MessageBD(int income, String Text, String date) {
        this.income = income;
        this.Text = Text;
        this.date=date;
    }

}