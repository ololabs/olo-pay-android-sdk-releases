// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data
import com.stripe.android.model.CardBrand as StripeCardBrand

/**
 * An enum representing card brands supported by Olo Pay
 *
 * @property description A string representation of the enum. Use this for the `cardtype` parameter
 * when submitting basket to the Olo Ordering API. If the value is [Unknown] basket submission will fail
 */
enum class CardBrand (val description: String) {
    /** Visa */
    Visa("Visa"),

    /** American Express */
    AmericanExpress("Amex"),

    /** Discover */
    Discover("Discover"),

    /** Mastercard */
    MasterCard("Mastercard"),

    /** Unknown card type */
    Unknown("Unknown");

    /** @suppress */
    companion object {
        internal fun convertFrom(brand: StripeCardBrand): CardBrand {
            return when (brand) {
                StripeCardBrand.Visa -> Visa
                StripeCardBrand.AmericanExpress -> AmericanExpress
                StripeCardBrand.Discover -> Discover
                StripeCardBrand.MasterCard -> MasterCard
                else -> Unknown
            }
        }
    }
}