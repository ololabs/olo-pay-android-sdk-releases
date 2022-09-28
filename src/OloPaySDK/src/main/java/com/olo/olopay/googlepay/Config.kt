// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

import com.stripe.android.googlepaylauncher.GooglePayPaymentMethodLauncher.BillingAddressConfig.Format as StripeBillingFormat

/**
 * Google Pay Configuration class
 *
 * @constructor Creates a new [Config] instance
 */
data class Config @JvmOverloads constructor(
    /** The environment Google Pay will be run in */
    val environment: Environment,

    /**
     * The company name displayed on the Google Pay sheet
     * This can be overridden in [GooglePayContext] if the displayed name needs to change based
     * on the store location
     */
    val companyName: String,

    /**
     * The default company country code
     * This can be overridden in [GooglePayContext] if there are store locations in multiple countries
     * */
    val companyCountryCode: String = "US",

    /**
     * If `true`, Google Pay is considered ready if the customer's Google Pay Wallet
     * has existing payment methods. Default value is `true`
     */
    val existingPaymentMethodRequired: Boolean = true,

    /**
     * Whether or not an email address is required to process the transaction. Default
     * value is `false`
     */
    val emailRequired: Boolean = false,

    /**
     * Whether or not a phone number is required to process the transaction. Default value is `false`
     */
    val phoneNumberRequired: Boolean = false,

    /**
     * The address format required to complete the transaction
     * @see AddressFormat
     */
    val addressFormat: AddressFormat = AddressFormat.Min
) {
    /**
     * Billing address fields required to complete the transaction
     */
    enum class AddressFormat(internal val code: String) {
        /** Name, country code, and postal code (Default) */
        Min("MIN"),

        /** Name, street address, locality, region, country code, and postal code */
        Full("FULL");

        internal fun toStripeFormat() : StripeBillingFormat {
            return when (this) {
                Min -> StripeBillingFormat.Min
                Full -> StripeBillingFormat.Full
            }
        }
    }
}