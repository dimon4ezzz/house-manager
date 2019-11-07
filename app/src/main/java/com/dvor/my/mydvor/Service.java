package com.dvor.my.mydvor;

public class Service {

    private String title;
    private String text;
    private String phone;
    private int imgResource;

    public Service(String title, String text, String phone){

        this.phone=phone;
        this.title=title;
        this.text=text;
     //   this.imgResource=img;
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
    public String getPhone() {
        return this.phone;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getImgResource() {
        return this.imgResource;
    }

    public void setImgResource(int imgResource) {
        this.imgResource = imgResource;
    }
}