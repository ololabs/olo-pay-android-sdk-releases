// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data
import com.stripe.android.model.CardBrand as StripeCardBrand

/**
 * An enum representing card brands supported by Olo Pay
 *
 * @property description A string representation of the enum. Use this for the `cardtype` parameter
 * when submitting basket to the Olo Ordering API. If the value is [Unsupported] or [Unknown] the
 * basket submission will fail
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

    /** Unsupported card type */
    Unsupported("Unsupported"),

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
                StripeCardBrand.JCB, StripeCardBrand.DinersClub, StripeCardBrand.UnionPay -> Unsupported
                else -> Unknown
            }
        }

        internal fun convertFrom(value: String): CardBrand {
            return when (value.uppercase()) {
                VISA_KEY -> Visa
                AMERICAN_EXPRESS_KEY -> AmericanExpress
                DISCOVER_KEY -> Discover
                MASTERCARD_KEY -> MasterCard
                INTERAC_KEY, JCB_KEY -> Unsupported
                else -> Unknown
            }
        }

        private const val VISA_KEY = "VISA"
        private const val AMERICAN_EXPRESS_KEY = "AMEX"
        private const val DISCOVER_KEY = "DISCOVER"
        private const val MASTERCARD_KEY = "MASTERCARD"
        private const val INTERAC_KEY = "INTERAC"
        private const val JCB_KEY = "JCB"
    }
}