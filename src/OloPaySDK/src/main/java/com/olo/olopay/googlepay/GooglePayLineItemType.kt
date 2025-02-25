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
}