package com.dvor.my.mydvor.data

class MessageBD {

    var income: Int = 0
    var Text: String = ""
    var date: String = ""

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    constructor(income: Int, Text: String, date: String) {
        this.income = income
        this.Text = Text
        this.date = date
    }

}