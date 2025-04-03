// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.internal.googlepay.GooglePayLauncherArgs
import com.olo.olopay.internal.googlepay.GooglePayLauncherResultContract
import com.olo.olopay.internal.googlepay.GooglePayRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * A drop-in class that presents a Google Pay sheet to collect a customer's payment details.
 * When successful, will return an [IPaymentMethod] via [GooglePayResult.Completed.paymentMethod]
 */
class GooglePayLauncher private constructor(
    context: Context,
    private val lifecycleScope: CoroutineScope,
    config: GooglePayConfig
): IGooglePayLauncher {
    private var _isReady = false
    private var _readyCallback: GooglePayReadyCallback? = null
    private var _resultCallback: GooglePayResultCallback? = null
    private val _googlePayRepository: GooglePayRepository = GooglePayRepository(context, config)
    private lateinit var _activityResultLauncher: ActivityResultLauncher<GooglePayLauncherArgs>

    /**
     * Constructor for using this class from a [ComponentActivity].
     * <hr class="spacer">
     *
     * #### Important:
     *
     * _This must be called no later than [ComponentActivity.onCreate]_
     *
     * @param activity The activity that is launching Google Pay
     * @param config Configuration parameters for Google Pay
     * @param readyCallback Called whenever the Google Pay readiness changes
     * @param resultCallback Called with the final result of the Google Pay payment operation
     */
    @JvmOverloads
    constructor(
        activity: ComponentActivity,
        config: GooglePayConfig,
        readyCallback: GooglePayReadyCallback? = null,
        resultCallback: GooglePayResultCallback? = null,
    ): this(
        activity as Context,
        activity.lifecycleScope,
        config
    ) {
        this.readyCallback = readyCallback
        this.resultCallback = resultCallback

        _activityResultLauncher = activity.registerForActivityResult(GooglePayLauncherResultContract()) {
            onGooglePayResult(it)
        }
    }

    /**
     * Constructor for using this class from a [Fragment].
     * <hr class="spacer">
     *
     * #### Important:
     * _This must be called no later than [Fragment.onViewCreated]_
     *
     * @param fragment The fragment that is launching Google Pay
     * @param config Configuration parameters for Google Pay
     * @param readyCallback Called whenever the Google Pay readiness changes
     * @param resultCallback Called with the final result of the Google Pay payment operation
     */
    @JvmOverloads
    constructor(
        fragment: Fragment,
        config: GooglePayConfig,
        readyCallback: GooglePayReadyCallback? = null,
        resultCallback: GooglePayResultCallback? = null,
    ): this(
        fragment.requireContext(),
        fragment.viewLifecycleOwner.lifecycleScope,
        config
    ) {
        this.readyCallback = readyCallback
        this.resultCallback = resultCallback

        _activityResultLauncher = fragment.registerForActivityResult(GooglePayLauncherResultContract()) {
            onGooglePayResult(it)
        }
    }

    init {
        emitOnGooglePayReady()
    }

    /**
     * Configuration parameters for Google Pay. When this is set, the ready state (see [isReady] and
     * [readyCallback]) may temporarily change to `false` until Google Pay gets reinitialized with
     * the new configuration parameters
     *
     * #### Important:
     * This property returns a copy of the config currently associated with this launcher. Changing
     * it will not be reflected in the launcher until the config property is set again.
     */
    override var config: GooglePayConfig
        get() = _googlePayRepository.config.copy()
        set(value) {
            _googlePayRepository.config = value
            emitOnGooglePayReady()
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
     * creation, but must be set prior to calling [GooglePayLauncher.present]
     */
    override var resultCallback: GooglePayResultCallback?
        get() = _resultCallback
        set(value) { _resultCallback = value }

    /**
     * True if Google Pay is available and ready to present
     */
    override val isReady
        get() = _isReady

    /**
     * Present the Google Pay UI.
     *
     * @param amount The amount intended to be collected. It should be a positive integer representing
     *               how much to charge in the smallest currency unit (e.g. 100 cents to charge $1.00).
     * @param checkoutStatus The status for the transaction. For [GooglePayCheckoutStatus.FinalImmediatePurchase], the pay button
     *                       text in the Google Pay sheet will be "Pay Now". For other statuses it will say "Continue"
     * @param totalPriceLabel A custom value to override the default total price label in the Google Pay sheet
     * @param lineItems A list of items to display in the Google Pay sheet.
     * @param validateLineItems If `true`, a [GooglePayException] will be thrown if the sum of the line items does not equal the total [amount] passed in.
     * @param transactionId A unique ID that identifies a transaction attempt. Merchants may use an existing
     *                      ID or generate a specific one for Google Pay transaction attempts. This field
     *                      is required when sending callbacks to the Google Transaction Events API
     * @throws GooglePayException Thrown if Google Pay isn't ready or if the configuration is not valid
     */
    override fun present(
        amount: Int,
        checkoutStatus: GooglePayCheckoutStatus,
        totalPriceLabel: String?,
        lineItems: List<GooglePayLineItem>?,
        validateLineItems: Boolean,
        transactionId: String?,
    ) {
        var exception: RuntimeException? = null
        var errorType = GooglePayErrorType.InternalError

        if (!isReady) {
            exception = RuntimeException("Google Pay is not ready yet")
            errorType = GooglePayErrorType.NotReadyError
        } else if (config.companyName.isEmpty()) {
            exception = RuntimeException("Company name cannot be empty")
            errorType = GooglePayErrorType.EmptyCompanyNameError
        } else if (config.companyCountryCode.isEmpty()) {
            exception = RuntimeException("Country code cannot be empty")
            errorType = GooglePayErrorType.EmptyCountryCodeError
        } else if (config.companyCountryCode.length != ValidCountryCodeLength) {
            exception = RuntimeException("Country code must contain 2 characters")
            errorType = GooglePayErrorType.InvalidCountryCodeError
        } else if(validateLineItems && !lineItems.isNullOrEmpty()) {
            val total = lineItems.sumOf { it.price }
            if(total != amount) {
                exception = RuntimeException("The total of the line items does not match the total amount")
                errorType = GooglePayErrorType.LineItemTotalMismatchError
            }
        }

        if (exception != null) {
            throw GooglePayException(exception, errorType)
        }

        _activityResultLauncher.launch(
            GooglePayLauncherArgs(
                config,
                amount,
                transactionId,
                checkoutStatus,
                if(totalPriceLabel.isNullOrEmpty()) "Pay ${config.companyName}" else totalPriceLabel,
                lineItems
            )
        )
    }

    /**
     * Present the Google Pay UI.
     *
     * @param amount The amount intended to be collected. It should be a positive integer representing
     *               how much to charge in the smallest currency unit (e.g. 100 cents to charge $1.00).
     * @param checkoutStatus The status for the transaction. For [GooglePayCheckoutStatus.FinalImmediatePurchase], the pay button
     *                       text in the Google Pay sheet will be "Pay Now". For other statuses it will say "Continue"
     * @param totalPriceLabel A custom value to override the default total price label in the Google Pay sheet
     * @param lineItems A list of items to display in the Google Pay sheet.
     * @param validateLineItems If `true`, a [GooglePayException] will be thrown if the sum of the line items does not equal the total [amount] passed in.
     * @throws GooglePayException Thrown if Google Pay isn't ready or if the configuration is not valid
     *
     */
    override fun present(
        amount: Int,
        checkoutStatus: GooglePayCheckoutStatus,
        totalPriceLabel: String?,
        lineItems: List<GooglePayLineItem>?,
        validateLineItems: Boolean
    ) {
        present(
            amount,
            checkoutStatus,
            totalPriceLabel,
            lineItems,
            validateLineItems,
            null
        )
    }
    
    /**
     * Present the Google Pay UI.
     *
     * @param amount The amount intended to be collected. It should be a positive integer representing
     *               how much to charge in the smallest currency unit (e.g. 100 cents to charge $1.00).
     * @param checkoutStatus The status for the transaction. For [GooglePayCheckoutStatus.FinalImmediatePurchase], the pay button
     *                       text in the Google Pay sheet will be "Pay Now". For other statuses it will say "Continue"
     * @param totalPriceLabel A custom value to override the default total price label in the Google Pay sheet
     * @param lineItems A list of items to display in the Google Pay sheet.
     *   **_Note:_** The sum total of the line items will be compared to the [amount] parameter. If they do not match, a [GooglePayException] will be thrown.
     *   If this validation is not desired, use a [present] method that takes the `validateLineItems` parameter.
     * @throws GooglePayException Thrown if Google Pay isn't ready or if the configuration is not valid
     */
    override fun present(
        amount: Int,
        checkoutStatus: GooglePayCheckoutStatus,
        totalPriceLabel: String?,
        lineItems: List<GooglePayLineItem>?
    ) {
        present(
            amount,
            checkoutStatus,
            totalPriceLabel,
            lineItems,
            true,
            null
        )
    }

    /**
     * Present the Google Pay UI.
     *
     * @param amount The amount intended to be collected. It should be a positive integer representing
     *               how much to charge in the smallest currency unit (e.g. 100 cents to charge $1.00).
     * @param checkoutStatus The status for the transaction. For [GooglePayCheckoutStatus.FinalImmediatePurchase], the pay button
     *                       text in the Google Pay sheet will be "Pay Now". For other statuses it will say "Continue"
     * @param totalPriceLabel A custom value to override the default total price label in the Google Pay sheet
     * @throws GooglePayException Thrown if Google Pay isn't ready or if the configuration is not valid
     */
    override fun present(
        amount: Int,
        checkoutStatus: GooglePayCheckoutStatus,
        totalPriceLabel: String?
    ) {
        present(
            amount,
            checkoutStatus,
            totalPriceLabel,
            null,
            true,
            null
        )
    }

    /**
     * Present the Google Pay UI.
     *
     * @param amount The amount intended to be collected. It should be a positive integer representing
     *               how much to charge in the smallest currency unit (e.g. 100 cents to charge $1.00).
     * @param checkoutStatus The status for the transaction. For [GooglePayCheckoutStatus.FinalImmediatePurchase], the pay button
     *                       text in the Google Pay sheet will be "Pay Now". For other statuses it will say "Continue"
     * @throws GooglePayException Thrown if Google Pay isn't ready or if the configuration is not valid
     */
    override fun present(amount: Int, checkoutStatus: GooglePayCheckoutStatus) {
        present(
            amount,
            checkoutStatus,
            null,
            null,
            true,
            null
        )
    }

    /**
     * Present the Google Pay UI.
     *
     * @param amount The amount intended to be collected. It should be a positive integer representing
     *               how much to charge in the smallest currency unit (e.g. 100 cents to charge $1.00).
     * @throws GooglePayException Thrown if Google Pay isn't ready or if the configuration is not valid
     */
    override fun present(amount: Int) {
        present(amount, GooglePayCheckoutStatus.FinalImmediatePurchase)
    }

    private fun onGooglePayReady(isReady: Boolean) {
        _isReady = isReady
        readyCallback?.onReady(isReady)
    }

    private fun onGooglePayResult(result: GooglePayResult) {
        resultCallback?.onResult(result)
    }

    private fun emitOnGooglePayReady() {
        lifecycleScope.launch {
            onGooglePayReady(_googlePayRepository.isReady().first())
        }
    }

    companion object {
        const val ValidCountryCodeLength = 2
    }
}