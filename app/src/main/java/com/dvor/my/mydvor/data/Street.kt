package com.dvor.my.mydvor.data

class Street(
        val id: String? = "",
        var name: String? = ""
) {
    override fun toString(): String {
        return name.toString()
    }
}