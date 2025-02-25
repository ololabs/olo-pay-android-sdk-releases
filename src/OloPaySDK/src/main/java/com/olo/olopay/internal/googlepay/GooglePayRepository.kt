// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.googlepay

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.olo.olopay.data.CurrencyCode
import com.olo.olopay.exceptions.ApiException
import com.olo.olopay.googlepay.GooglePayCheckoutStatus
import com.olo.olopay.googlepay.GooglePayConfig
import com.olo.olopay.googlepay.GooglePayLineItem
import com.olo.olopay.googlepay.GooglePayEnvironment
import com.olo.olopay.internal.extensions.toStripeCheckoutOption
import com.olo.olopay.internal.extensions.toStripePriceStatus
import com.stripe.android.GooglePayJsonFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import org.json.JSONArray
import org.json.JSONObject

internal class GooglePayRepository internal constructor(
    context: Context,
    config: GooglePayConfig
) {
    private var _config: GooglePayConfig
    private val _context = context.applicationContext
    private val _googlePayJsonFactory = GooglePayJsonFactory(context)
    private var _paymentsClient: PaymentsClient? = null

    init {
        _config = config
    }

    private val paymentsClient: PaymentsClient
        get() {
            if (_paymentsClient == null) {
                val environment = if (config.environment == GooglePayEnvironment.Test) WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION

                val options = Wallet.WalletOptions.Builder()
                    .setEnvironment(environment)
                    .build()

                _paymentsClient = Wallet.getPaymentsClient(_context, options)
            }

            return _paymentsClient!!
        }

    internal var config: GooglePayConfig
        get() = _config
        set(value) {
            _config = value
            _paymentsClient = null
        }

    private fun getBillingAddressParameters(): GooglePayJsonFactory.BillingAddressParameters? {
        val addressFormat = if (config.fullBillingAddressRequired) {
            GooglePayJsonFactory.BillingAddressParameters.Format.Full
        } else {
            GooglePayJsonFactory.BillingAddressParameters.Format.Min
        }

        return GooglePayJsonFactory.BillingAddressParameters(
            isRequired = true,
            format = addressFormat,
            isPhoneNumberRequired = config.phoneNumberRequired
        )
    }

    internal fun isReady(): Flow<Boolean> {
        val isReadyState = MutableStateFlow<Boolean?>(null)

        val request = IsReadyToPayRequest.fromJson(
            _googlePayJsonFactory.createIsReadyToPayRequest(
                billingAddressParameters = getBillingAddressParameters(),
                existingPaymentMethodRequired = config.existingPaymentMethodRequired,
                allowCreditCards = true
            ).toString()
        )

        paymentsClient.isReadyToPay(request).addOnCompleteListener { task ->
            val isReady = runCatching {
                task.getResult(ApiException::class.java) == true
            }.getOrDefault(false)

            isReadyState.value = isReady
        }

        return isReadyState.filterNotNull()
    }

    internal fun createPaymentDataTask(transactionId: String?, amount: Int?, checkoutStatus: GooglePayCheckoutStatus, totalPriceLabel: String, lineItems: List<GooglePayLineItem>?): Task<PaymentData> {
        return paymentsClient.loadPaymentData(
            PaymentDataRequest.fromJson(createPaymentDataRequest(transactionId, amount, checkoutStatus, totalPriceLabel, lineItems).toString())
        )
    }

    private fun createPaymentDataRequest(
        transactionId: String?,
        amount: Int?,
        checkoutStatus: GooglePayCheckoutStatus,
        totalPriceLabel: String,
        lineItems: List<GooglePayLineItem>?,
    ): JSONObject {
        var paymentDataRequest = _googlePayJsonFactory.createPaymentDataRequest(
            transactionInfo = GooglePayJsonFactory.TransactionInfo(
                currencyCode = config.currencyCode.code,
                totalPriceStatus = checkoutStatus.toStripePriceStatus(),
                countryCode = config.companyCountryCode,
                transactionId = transactionId,
                totalPrice = amount,
                totalPriceLabel = totalPriceLabel,
                checkoutOption = checkoutStatus.toStripeCheckoutOption()
            ),
            merchantInfo = GooglePayJsonFactory.MerchantInfo(merchantName = config.companyName),
            billingAddressParameters = getBillingAddressParameters(),
            isEmailRequired = config.emailRequired,
            allowCreditCards = true
        )

        if(!lineItems.isNullOrEmpty()) {
            var lineItemsJsonArray = JSONArray()

            lineItems.forEach {
                lineItemsJsonArray.put(it.toJson(config.currencyCode))
            }
            paymentDataRequest.getJSONObject(TRANSACTION_INFO_KEY).put(DISPLAY_ITEMS_KEY, lineItemsJsonArray)
        }

        return paymentDataRequest
    }

    companion object {
        // DO NOT CHANGE THESE VALUES - THEY MAP DIRECTLY TO WHAT IS EXPECTED BY THE GOOGLE PAY API
        private const val TRANSACTION_INFO_KEY = "transactionInfo"
        private const val DISPLAY_ITEMS_KEY = "displayItems"
    }
}