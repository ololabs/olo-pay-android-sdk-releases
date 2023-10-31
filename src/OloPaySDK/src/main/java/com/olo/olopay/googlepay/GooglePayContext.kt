// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import com.olo.olopay.api.IOloPayApiInitializer
import com.olo.olopay.exceptions.GooglePayException
import java.lang.IllegalStateException
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.internal.googlepay.GooglePayLauncher

/**
 * A drop-in class that presents a Google Pay sheet to collect a customer's payment details.
 * When successful, will return an [IPaymentMethod] via [Result.Completed.paymentMethod]
 */
class GooglePayContext: IGooglePayContext {
    private var _isReady = false
    private val _googlePayLauncher: GooglePayLauncher
    private var _readyCallback: ReadyCallback? = null

    /**
     * Constructor for using this class from an Activity.
     * <hr class="spacer">
     *
     * #### Important:
     *
     * _This must be called no later than [ComponentActivity.onCreate]_
     *
     * @param activity The activity that is launching Google Pay
     * @param readyCallback Called after determining whether Google Pay is available and ready on the device
     * @param resultCallback Called with the final result of the Google Pay payment operation
     * @param merchantName Specify this if you want to override the name set up when intiializing the SDK
     * @param merchantCountryCode Specify this if you want to override the country code set up when initializing the SDK
     * @throws GooglePayException Thrown if Google Pay wasn't configured when initializing the SDK
     */
    @JvmOverloads
    constructor(
        activity: ComponentActivity,
        readyCallback: ReadyCallback? = null,
        resultCallback: ResultCallback? = null,
        merchantName: String? = null,
        merchantCountryCode: String? = null) {

        this.readyCallback = readyCallback
        this.resultCallback = resultCallback

        _googlePayLauncher = GooglePayLauncher(activity, getGooglePayConfig(merchantName, merchantCountryCode))
        _googlePayLauncher.readyCallback = ReadyCallback(::onGooglePayReady)
        _googlePayLauncher.resultCallback = ResultCallback(::onGooglePayResult)
    }

    /**
     * Constructor for using this class from a [Fragment].
     * <hr class="spacer">
     *
     * ####Important:
     *
     * _ This must be called no later than [Fragment.onViewCreated]_
     *
     * @param fragment The fragment that is launching Google Pay
     * @param readyCallback Called after determining whether Google Pay is available and ready on the device
     * @param resultCallback Called with the final result of the Google Pay payment operation
     * @param merchantName Specify this if you want to override the name set up when initializing the SDK
     * @param merchantCountryCode Specify this if you want to override the country code set up when initializing the SDK
     * @throws GooglePayException Thrown if Google Pay wasn't configured when initializing the SDK
     */
    @JvmOverloads
    constructor(
        fragment: Fragment,
        readyCallback: ReadyCallback? = null,
        resultCallback: ResultCallback? = null,
        merchantName: String? = null,
        merchantCountryCode: String? = null) {

        this.readyCallback = readyCallback
        this.resultCallback = resultCallback

        _googlePayLauncher = GooglePayLauncher(fragment, getGooglePayConfig(merchantName, merchantCountryCode))
        _googlePayLauncher.readyCallback = ReadyCallback(::onGooglePayReady)
        _googlePayLauncher.resultCallback = ResultCallback(::onGooglePayResult)
    }

    /**
     * Optional callback to be notified when Google Pay is ready. Alternatively, you can check if
     * Google Pay is ready by checking the [isReady] property
     */
    override var readyCallback
        get() = _readyCallback
        set(value) {
            _readyCallback = value
            _readyCallback?.onReady(_isReady)
        }

    /**
     * Callback to get the results of the Google Pay flow. If this isn't set there is no way to get
     * the resulting payment method. It can be passed in to the constructor or manually set after
     * creation, but must be set prior to calling [GooglePayContext.present]
     */
    override var resultCallback: ResultCallback?

    /**
     * True if Google Pay is available and ready to present
     */
    override val isReady
        get() = _isReady

    /**
     * Present the Google Pay UI. If [isReady] is `false` then this method is a no-op
     *
     * @param currencyCode The ISO-4217 alphabetic currency code (e.g. USD, EUR). Default is "USD"
     * @param amount The amount intended to be collected. It should be a positive integer representing
     *               how much to charge in the smallest currency unit (e.g. 100 cents to charge $1.00).
     *               If the amount is not yet known, use 0
     * @param transactionId A unique ID that identifies a transaction attempt. Merchants may use an existing
     *                      ID or generate a specific one for Google Pay transaction attempts. This field
     *                      is required when sending callbacks to the Google Transaction Events API
     */
    override fun present(currencyCode: String, amount: Int, transactionId: String?) {
        if (!isReady) {
            return
        }

        _googlePayLauncher.present(currencyCode, amount, transactionId)
    }

    /**
     * Present the Google Pay UI. If [isReady] is `false` then this method is a no-op
     *
     * @param currencyCode The ISO-4217 alphabetic currency code (e.g. USD, EUR). Default is "USD"
     * @param amount The amount intended to be collected. It should be a positive integer representing
     *               how much to charge in the smallest currency unit (e.g. 100 cents to charge $1.00).
     *               If the amount is not yet known, use 0
     */
    override fun present(currencyCode: String, amount: Int) {
        present(currencyCode, amount, IGooglePayContext.defaultTransactionId)
    }

    /**
     * Present the Google Pay UI. If [isReady] is `false` then this method is a no-op
     *
     * @param currencyCode The ISO-4217 alphabetic currency code (e.g. USD, EUR). Default is "USD"
     */
    override fun present(currencyCode: String) {
        present(currencyCode, IGooglePayContext.defaultAmount, IGooglePayContext.defaultTransactionId)
    }

    /**
     * Present the Google Pay UI. If [isReady] is `false` then this method is a no-op
     */
    override fun present() {
        present(IGooglePayContext.defaultCurrency, IGooglePayContext.defaultAmount, IGooglePayContext.defaultTransactionId)
    }

    private fun getGooglePayConfig(merchantName: String?, merchantCountryCode: String?): Config {
        if (IOloPayApiInitializer.googlePayConfig == null) {
            val message = "Google Pay is not configured. Call OloPayAPI.init() with Google Pay configuration parameters"
            throw GooglePayException(IllegalStateException(message), GooglePayErrorType.DeveloperError)
        }

        return IOloPayApiInitializer.googlePayConfig!!.copy(
            companyName = merchantName ?: IOloPayApiInitializer.googlePayConfig!!.companyName,
            companyCountryCode = merchantCountryCode ?: IOloPayApiInitializer.googlePayConfig!!.companyCountryCode
        )
    }

    private fun onGooglePayReady(isReady: Boolean) {
        _isReady = isReady
        readyCallback?.onReady(isReady)
    }

    private fun onGooglePayResult(result: Result) {
        resultCallback?.onResult(result)
    }
}