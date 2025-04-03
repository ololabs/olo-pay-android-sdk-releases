// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

internal const val DefaultCheckoutOption = "DEFAULT"
internal const val ImmediatePurchaseCheckoutOption = "COMPLETE_IMMEDIATE_PURCHASE"
internal const val EstimatedPrice = "ESTIMATED"
internal const val FinalPrice = "FINAL"

// These options and values are defined by Google Pay's TransactionInfo object and should
// not be changed. The values correspond to the totalPriceStatus and checkoutOption fields.
// Since the allowed values depend on each other, we are combining them into one enum to
// prevent states that are not allowed.
// For more info: https://developers.google.com/pay/api/android/reference/request-objects#TransactionInfo

/**
 * Enum representing valid checkout option and price status combinations, as defined by Google.
 * For more details, see the `totalPriceStatus` and `checkoutOption` properties of Google's [TransactionInfo](https://developers.google.com/pay/api/web/reference/request-objects#TransactionInfo) object.
 */
enum class GooglePayCheckoutStatus(val priceStatus: String, val checkoutOption: String) {
    /**
     * Represents an estimated price (meaning it's not final and could change) and the default checkout option.
     * For more details, see the `totalPriceStatus` and `checkoutOption` properties of Google's [TransactionInfo](https://developers.google.com/pay/api/web/reference/request-objects#TransactionInfo) object.
     *
     * This value will cause the text of the Google Pay sheet confirmation button to display "Continue"
     */
    EstimatedDefault(EstimatedPrice, DefaultCheckoutOption),

    /**
     * Represents the final price of the transaction and the default checkout option.
     * For more details, see the `totalPriceStatus` and `checkoutOption` properties of Google's [TransactionInfo](https://developers.google.com/pay/api/web/reference/request-objects#TransactionInfo) object.
     *
     * This value will cause the text of the Google Pay sheet confirmation button to display "Continue"
     */
    FinalDefault(FinalPrice, DefaultCheckoutOption),

    /**
     * Represents the final price of the transaction and the immediate checkout option.
     * For more details, see the `totalPriceStatus` and `checkoutOption` properties of Google's [TransactionInfo](https://developers.google.com/pay/api/web/reference/request-objects#TransactionInfo) object.
     *
     * This value will cause the text of the Google Pay sheet confirmation button to display "Pay Now"
     */
    FinalImmediatePurchase(FinalPrice, ImmediatePurchaseCheckoutOption)
}