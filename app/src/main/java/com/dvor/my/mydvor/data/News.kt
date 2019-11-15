package com.dvor.my.mydvor.data

class News(
        val id: String,
        var title: String?,
        var text: String?,
        internal var date: String?,
        val imgResource: String,
        val uid: String?
)