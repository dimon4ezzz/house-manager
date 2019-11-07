package com.dvor.my.mydvor;

public class Stock {

    private String title;
    private String text;
    private String address;
    private String img;

    public Stock(String title, String text, String address, String img){

        this.title=title;
        this.text=text;
        this.address=address;
        this.img=img;
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

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImgResource() {
        return this.img;
    }

    public void setImgResource(String imgResource) {
        this.img = imgResource;
    }
}