// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

/** Interface for mocking/testing purposes */
interface IGooglePayContext {
    /** Whether or not Google Pay is ready. See [GooglePayContext] for documentation **/
    val isReady: Boolean

    /** Present the Google Pay UI. See [GooglePayContext] for documentation **/
    fun present(currencyCode: String = defaultCurrency, amount: Int = defaultAmount, transactionId: String? = defaultTransactionId)

    /** Present the Google Pay UI. See [GooglePayContext] for documentation **/
    fun present(currencyCode: String = defaultCurrency, amount: Int = defaultAmount)

    /** Present the Google Pay UI. See [GooglePayContext] for documentation **/
    fun present(currencyCode: String = defaultCurrency)

    /** Present the Google Pay UI. See [GooglePayContext] for documentation **/
    fun present()

    /** Callback for when Google Pay is ready. See [GooglePayContext] for documentation **/
    var readyCallback: ReadyCallback?

    /** Callback for getting results from the Google Pay flow. See [GooglePayContext] for documentation **/
    var resultCallback: ResultCallback?

    /** Default values for constructor parameters **/
    companion object Defaults {
        /** Default amount for constructors that don't take an amount */
        const val defaultAmount = 0

        /** Default currency for constructors that don't take a currency */
        const val defaultCurrency = "USD"

        /** Default transaction id for constructors that don't take a transaction id */
        val defaultTransactionId: String? = null
    }
}