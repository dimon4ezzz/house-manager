package com.dvor.my.mydvor;

public class News {

    private String ID;
    private String date;
    private String title;
    private String text;
    private int likesCount;
    private boolean liked;
    private String imgResource;
    private String uID;


    public News(String ID, String title, String text, String date, String img, String uID){

        this.ID=ID;
        this.title=title;
        this.text=text;
        this.date=date;
        this.imgResource=img;
        this.uID=uID;

    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String title) {
        this.date = date;
    }


    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLikesCount() {
        return this.likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public boolean getLiked() {
        return this.liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getImgResource() {
        return this.imgResource;
    }

    public String getUID() {
        return this.uID;
    }
    public String getID() {
        return this.ID;
    }

}