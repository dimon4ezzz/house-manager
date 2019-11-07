package com.dvor.my.mydvor.data;

public class Message {

    private String title;
    private String text;
    private String date;

    public Message(String title, String text, String date){

        this.title=title;
        this.text=text;
        this.date=date;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
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

