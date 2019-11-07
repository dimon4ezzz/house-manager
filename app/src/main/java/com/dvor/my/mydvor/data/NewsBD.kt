package com.dvor.my.mydvor.data

class NewsBD {

    var header: String = ""//authorID;
    var text: String = ""
    var date: String = ""
    var img: String = ""
    var uID: String = ""

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    constructor(header: String, text: String, date: String, img: String,
                uID: String) {
        this.header = header
        this.text = text
        this.date = date
        this.img = img
        this.uID = uID
    }

}