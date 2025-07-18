// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

/** Interface for mocking/testing purposes */
interface IGooglePayLauncher {
    /** Whether or not Google Pay is ready. See [GooglePayLauncher] for documentation **/
    val isReady: Boolean

    /** Present the Google Pay UI. See [GooglePayLauncher] for documentation **/
    fun present(
        amount: Int,
        checkoutStatus: GooglePayCheckoutStatus,
        totalPriceLabel: String?,
        lineItems: List<GooglePayLineItem>?,
        validateLineItems: Boolean,
        transactionId: String?,
    )

    /** Present the Google Pay UI. See [GooglePayLauncher] for documentation **/
    fun present(
        amount: Int,
        checkoutStatus: GooglePayCheckoutStatus,
        totalPriceLabel: String?,
        lineItems: List<GooglePayLineItem>?,
        validateLineItems: Boolean,
    )

    /** Present the Google Pay UI. See [GooglePayLauncher] for documentation **/
    fun present(
        amount: Int,
        checkoutStatus: GooglePayCheckoutStatus,
        totalPriceLabel: String?,
        lineItems: List<GooglePayLineItem>?,
    )

    /** Present the Google Pay UI. See [GooglePayLauncher] for documentation **/
    fun present(
        amount: Int,
        checkoutStatus: GooglePayCheckoutStatus,
        totalPriceLabel: String?
    )

    /** Present the Google Pay UI. See [GooglePayLauncher] for documentation **/
    fun present(
        amount: Int,
        checkoutStatus: GooglePayCheckoutStatus
    )

    /** Present the Google Pay UI. See [GooglePayLauncher] for documentation **/
    fun present(amount: Int)

    /** Callback for when Google Pay is ready. See [GooglePayLauncher] for documentation **/
    var readyCallback: GooglePayReadyCallback?

    /** Callback for getting results from the Google Pay flow. See [GooglePayLauncher] for documentation **/
    var resultCallback: GooglePayResultCallback?

    /** Configuration parameters for Google Pay. See [GooglePayLauncher] for documentation **/
    var config: GooglePayConfig
}