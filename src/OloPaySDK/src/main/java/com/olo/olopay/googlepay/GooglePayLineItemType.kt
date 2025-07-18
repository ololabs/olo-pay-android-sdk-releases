// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

/**
 * Represents the type of line item to be displayed in the Google Pay sheet.
 */
enum class GooglePayLineItemType(val value: String) {
    /** Represents a subtotal line item */
    Subtotal("SUBTOTAL"),

    /** Represents a line item */
    LineItem("LINE_ITEM"),

    /** Represents a tax line item */
    Tax("TAX");

    companion object {
        /** Convenience method to convert a string to a GooglePayLineItemType */
        public fun from(value: String): GooglePayLineItemType? {
            return when (value.uppercase()) {
                Subtotal.value -> Subtotal
                Tax.value -> Tax
                LineItem.value -> LineItem
                else -> null
            }
        }
    }
}