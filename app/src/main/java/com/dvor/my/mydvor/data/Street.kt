package com.dvor.my.mydvor.data

data class Street(
        val id: String = "",
        var name: String = ""
) {
    /**
     * Must be override, because Registration use `toString` method
     */
    override fun toString(): String =
            name
}