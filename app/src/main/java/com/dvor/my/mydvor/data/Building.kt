package com.dvor.my.mydvor.data

data class Building(
        val id: String?,
        var number: String?
) {
    override fun toString(): String {
        return number.toString()
    }
}