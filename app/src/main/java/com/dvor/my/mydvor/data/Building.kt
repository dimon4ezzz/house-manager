package com.dvor.my.mydvor.data

data class Building(
        val id: String,
        var number: String
) {
    /**
     * It is for spinners.
     *
     * @see com.dvor.my.mydvor.RegistrationActivity
     * @see com.dvor.my.mydvor.settings.SettingsFragment
     */
    override fun toString(): String =
            number
}