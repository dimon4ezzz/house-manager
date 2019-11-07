package com.dvor.my.mydvor;

public class Notification {

    private String text;
    private String date;

    public Notification(String text, String date){

        this.text=text;
        this.date=date;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getData() {
        return this.date;
    }

    public void setData(String date) {
        this.date = date;
    }
}

