package com.dvor.my.mydvor.data

data class Street(
        val id: String = "",
        var name: String = ""
) {
    /**
     * It is for spinners.
     *
     * @see com.dvor.my.mydvor.RegistrationActivity
     * @see com.dvor.my.mydvor.settings.SettingsFragment
     */
    override fun toString(): String =
            name
}