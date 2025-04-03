package com.olo.olopay.googlepay

import com.google.android.gms.wallet.button.ButtonConstants

/**
 * An enum representing different visual styles available for a Google Pay button
 */
enum class GooglePayButtonTheme(
    val value: Int
) {
    /** A light-themed button **/
    Light(ButtonConstants.ButtonTheme.LIGHT),

    /** A dark-themed button **/
    Dark(ButtonConstants.ButtonTheme.DARK);

    companion object {
        /**
         * Get an instance of this enum based on a string.
         * @param value The case-insensitive string value of the desired enum
         * @return An enum based on the passed in [value] or [Dark] if no match was found
         */
        fun convertFrom(value: String) : GooglePayButtonTheme {
            return when (value.uppercase()) {
                LIGHT_KEY -> Light
                DARK_KEY -> Dark
                else -> Dark
            }
        }

        internal fun convertFrom(value: Int) : GooglePayButtonTheme {
            return when (value) {
                LIGHT_ATTR_VALUE -> Light
                DARK_ATTR_VALUE -> Dark
                else -> Dark
            }
        }

        internal const val LIGHT_KEY = "LIGHT"
        internal const val DARK_KEY = "DARK"

        // NOTE: These values map to values defined in attrs.xml
        internal const val DARK_ATTR_VALUE = 1
        internal const val LIGHT_ATTR_VALUE = 2
    }
}