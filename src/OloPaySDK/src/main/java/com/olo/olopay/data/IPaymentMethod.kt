// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

import android.os.Parcelable
import com.olo.olopay.googlepay.GooglePayConfig

/**
 * Represents a payment method containing all information needed to submit a basket
 * via Olo's Ordering API
 */
interface IPaymentMethod: Parcelable {
    /**
     * The payment method id. This should be set to the token field when submitting a basket
     */
    val id: String

    /**
     * The last four digits of the card
     */
    val last4: String

    /**
     * The issuer of the card (e.g. Visa, Mastercard, etc)
     */
    val cardType: CardBrand

    /**
     * Two-digit number representing the card’s expiration month
     */
    val expirationMonth: Int?

    /**
     * Four-digit number representing the card’s expiration year
     */
    val expirationYear: Int?

    /**
     * Convenience property for accessing the Zip or postal code, equivalent to `billingAddress.postalCode`
     */
    val postalCode: String

    /**
     * Convenience property for accessing the country code, equivalent to `billingAddress.countryCode`
     */
    val countryCode: String

    /**
     * The email associated with the transaction. If [isGooglePay] is `false` or if Google Pay was
     * not configured to require an email address, this will be an empty string
     */
    val email: String

    /**
     * User-facing description of the payment method as provided by Google. If [isGooglePay]
     * is `false`, this will be an empty string
     */
    val googlePayCardDescription: String

    /**
     * The billing address associated with the transaction. The country code and postal code will
     * always be set. Other address fields will only be set if [isGooglePay] is `true` and will
     * depend on [GooglePayConfig.fullBillingAddressRequired]
     */
    val billingAddress: Address

    /**
     * The name associated with the transaction. Will only be provided if [isGooglePay]
     * and [GooglePayConfig.fullNameRequired] are `true`
     */
    val fullName: String

    /**
     * The phone number associated with the transaction. Will only be provided if [isGooglePay]
     * and [GooglePayConfig.phoneNumberRequired] are `true`
     */
    val phoneNumber: String

    /**
     * Whether or not this payment method was created via Google Pay
     */
    val isGooglePay: Boolean

    /**
     * The environment used to create the payment method
     */
    val environment: OloPayEnvironment
}