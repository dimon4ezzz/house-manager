package com.dvor.my.mydvor;

public class NewsBD {

    public String header;//authorID;
    public String text;
    public String date;
    public String img;
    public String uID;
    public NewsBD() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public NewsBD( String  header, String text, String date, String img,
                  String uID) {
        this. header =  header;
        this.text = text;
        this.date=date;
        this.img=img;
        this.uID=uID;
    }

}