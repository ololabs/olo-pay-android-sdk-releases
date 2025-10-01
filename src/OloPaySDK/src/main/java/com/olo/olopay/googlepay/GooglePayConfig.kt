// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

import android.os.Parcelable
import com.olo.olopay.data.CurrencyCode
import kotlinx.parcelize.Parcelize

/**
 * Google Pay Configuration class
 *
 * @constructor Creates a new [GooglePayConfig] instance
 */
@Parcelize
data class GooglePayConfig @JvmOverloads constructor(
    /** The environment Google Pay will be run in */
    val environment: GooglePayEnvironment,

    /**
     * The merchant/vendor display name. This will usually show up in the Google Pay sheet next to the total
     */
    val companyName: String,

    /**
     * A two character country code for the vendor that will be processing transactions. Default value is "US"
     */
    val companyCountryCode: String = "US",

    /**
     * Whether an existing saved payment method is required for Google Pay to be considered ready. Default
     * value is `false`
     *
     * Since cards can be added within the Google Pay sheet, it is recommended to set this to `false`
     */
    val existingPaymentMethodRequired: Boolean = false,

    /**
     * Whether Google Pay collects and returns an email address when processing transactions. Default value is `false`
     */
    val emailRequired: Boolean = false,

    /**
     * Whether Google Pay collects and returns a phone number when processing transactions. Default value is `false`
     */
    val phoneNumberRequired: Boolean = false,

    /**
     * Whether Google Pay collects and returns a name when processing transactions. Default value is `false`
     */
    val fullNameRequired: Boolean = false,

    /**
     * Whether Google Pay collects and returns a full billing address when processing transactions.
     * If `false`, only postal code and country code will be provided. Default value is `false`
     */
    val fullBillingAddressRequired: Boolean = false,

    /**
     * The currency code to use for Google Pay transactions. Default value is [CurrencyCode.USD]
     */
    val currencyCode: CurrencyCode = CurrencyCode.USD
): Parcelable