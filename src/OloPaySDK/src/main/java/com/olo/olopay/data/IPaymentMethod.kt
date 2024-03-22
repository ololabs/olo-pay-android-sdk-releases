// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

import android.os.Parcelable

/**
 * Represents a payment method containing all information needed to submit a basket
 * via Olo's Ordering API
 */
interface IPaymentMethod: Parcelable {
    /**
     * The payment method id. This should be set to the token field when submitting a basket
     */
    val id: String?

    /**
     * The last four digits of the card
     */
    val last4: String?

    /**
     * The issuer of the card (e.g. Visa, Mastercard, etc)
     */
    val cardType: CardBrand?

    /**
     * Two-digit number representing the card’s expiration month
     */
    val expirationMonth: Int?

    /**
     * Four-digit number representing the card’s expiration year
     */
    val expirationYear: Int?

    /**
     * Zip or postal code
     */
    val postalCode: String?

    /**
     * 2-digit country code
     */
    val country: String?

    /**
     * Whether or not this payment method was created via Google Pay
     */
    val isGooglePay: Boolean

    /**
     * The environment used to create the payment method
     */
    val environment: OloPayEnvironment
}