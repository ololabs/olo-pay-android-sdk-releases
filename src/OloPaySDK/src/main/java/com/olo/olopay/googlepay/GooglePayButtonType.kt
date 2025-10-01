// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

import com.google.android.gms.wallet.button.ButtonConstants
import com.olo.olopay.googlepay.GooglePayButtonTheme.Companion.DARK_ATTR_VALUE
import com.olo.olopay.googlepay.GooglePayButtonTheme.Companion.LIGHT_ATTR_VALUE
import com.olo.olopay.googlepay.GooglePayButtonTheme.Dark
import com.olo.olopay.googlepay.GooglePayButtonTheme.Light

/**
 * An enum representing different types of Google Pay buttons that can be used
 * Values map directly to Google's [ButtonType](https://developers.google.com/android/reference/com/google/android/gms/wallet/button/ButtonConstants.ButtonType)
 */
public enum class GooglePayButtonType(
    val value: Int
) {
    /**
     * A button that uses the phrase "Book with" in conjunction with the Google Pay logo
     */
    Book(ButtonConstants.ButtonType.BOOK),

    /**
     * A button that uses the phrase "Buy with" in conjunction with the Google Pay logo
     */
    Buy(ButtonConstants.ButtonType.BUY),

    /**
     * A button that uses the phrase "Checkout with" in conjunction with the Google Pay logo
     */
    Checkout(ButtonConstants.ButtonType.CHECKOUT),

    /**
     * A button that uses the phrase "Donate with" in conjunction with the Google Pay logo
     */
    Donate(ButtonConstants.ButtonType.DONATE),

    /**
     * A button that uses the phrase "Order with" in conjunction with the Google Pay logo
     */
    Order(ButtonConstants.ButtonType.ORDER),

    /**
     * A button that uses the phrase "Pay with" in conjunction with the Google Pay logo
     */
    Pay(ButtonConstants.ButtonType.PAY),

    /**
     * A button with the Google Pay logo only
     */
    Plain(ButtonConstants.ButtonType.PLAIN),

    /**
     * A button that uses the phrase "Subscribe with" in conjunction with the Google Pay logo
     */
    Subscribe(ButtonConstants.ButtonType.SUBSCRIBE);

    companion object {
        /**
         * Get an instance of this enum based on a string.
         * @param value The case-insensitive string value of the desired enum
         * @return An enum based on the passed in [value] or [Checkout] if no match was found
         */
        fun convertFrom(value: String) : GooglePayButtonType {
            return when (value.uppercase()) {
                BOOK_KEY -> Book
                BUY_KEY -> Buy
                CHECKOUT_KEY -> Checkout
                DONATE_KEY -> Donate
                ORDER_KEY -> Order
                PAY_KEY -> Pay
                PLAIN_KEY -> Plain
                SUBSCRIBE_KEY -> Subscribe
                else -> Checkout
            }
        }

        internal fun convertFrom(value: Int) : GooglePayButtonType {
            return when (value) {
                BOOK_ATTR_VALUE -> Book
                BUY_ATTR_VALUE -> Buy
                CHECKOUT_ATTR_VALUE -> Checkout
                DONATE_ATTR_VALUE -> Donate
                ORDER_ATTR_VALUE -> Order
                PAY_ATTR_VALUE -> Pay
                PLAIN_ATTR_VALUE -> Plain
                SUBSCRIBE_ATTR_VALUE -> Subscribe
                else -> Checkout
            }
        }

        internal const val BOOK_KEY = "BOOK"
        internal const val BUY_KEY = "BUY"
        internal const val CHECKOUT_KEY = "CHECKOUT"
        internal const val DONATE_KEY = "DONATE"
        internal const val ORDER_KEY = "ORDER"
        internal const val PAY_KEY = "PAY"
        internal const val PLAIN_KEY = "PLAIN"
        internal const val SUBSCRIBE_KEY = "SUBSCRIBE"

        // NOTE: These values map to values defined in attrs.xml
        internal const val BUY_ATTR_VALUE = 1
        internal const val BOOK_ATTR_VALUE = 2
        internal const val CHECKOUT_ATTR_VALUE = 3
        internal const val DONATE_ATTR_VALUE = 4
        internal const val ORDER_ATTR_VALUE = 5
        internal const val PAY_ATTR_VALUE = 6
        internal const val SUBSCRIBE_ATTR_VALUE = 7
        internal const val PLAIN_ATTR_VALUE = 8
    }
}