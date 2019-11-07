package com.dvor.my.mydvor.data

class News(val id: String, var title: String?, var text: String?, internal var date: String?, val imgResource: String, val uid: String) {
    var likesCount: Int = 0
    var liked: Boolean = false

    fun getDate(): String? {
        return this.date
    }

    fun setDate(title: String) {
    }

}