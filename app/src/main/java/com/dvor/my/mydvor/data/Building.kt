package com.dvor.my.mydvor.data

data class Building(
        val id: String,
        var number: String
) {
    /**
     * Must be override, because Registration use `toString` method
     */
    override fun toString(): String =
            number
}