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
import com.olo.olopay.exceptions.ApiException
import com.olo.olopay.googlepay.Config
import com.olo.olopay.googlepay.Environment
import com.stripe.android.GooglePayJsonFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import org.json.JSONObject

internal class GooglePayRepository internal constructor(
    context: Context,
    private val config: Config
) {
    private val _context = context.applicationContext
    private val _googlePayJsonFactory = GooglePayJsonFactory(context)

    private val paymentsClient: PaymentsClient by lazy {
        val environment = if (config.environment == Environment.Test) WalletConstants.ENVIRONMENT_TEST else WalletConstants.ENVIRONMENT_PRODUCTION

        val options = Wallet.WalletOptions.Builder()
            .setEnvironment(environment)
            .build()

        Wallet.getPaymentsClient(_context, options)
    }

    internal fun isReady(): Flow<Boolean> {
        val isReadyState = MutableStateFlow<Boolean?>(null)

        val billingAddressParameters = GooglePayJsonFactory.BillingAddressParameters(
            format = config.addressFormat.toStripeGooglePayFormat(),
            isPhoneNumberRequired = config.phoneNumberRequired
        )

        val request = IsReadyToPayRequest.fromJson(
            _googlePayJsonFactory.createIsReadyToPayRequest(
                billingAddressParameters = billingAddressParameters,
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

    internal fun createPaymentDataTask(currencyCode: String, transactionId: String?, amount: Int?): Task<PaymentData> {
        return paymentsClient.loadPaymentData(
            PaymentDataRequest.fromJson(createPaymentDataRequest(currencyCode, transactionId, amount).toString())
        )
    }

    private fun createPaymentDataRequest(
        currencyCode: String,
        transactionId: String?,
        amount: Int?
    ): JSONObject {
        return _googlePayJsonFactory.createPaymentDataRequest(
            transactionInfo = GooglePayJsonFactory.TransactionInfo(
                currencyCode = currencyCode,
                totalPriceStatus = GooglePayJsonFactory.TransactionInfo.TotalPriceStatus.Estimated,
                countryCode = config.companyCountryCode,
                transactionId = transactionId,
                totalPrice = amount,
                checkoutOption = GooglePayJsonFactory.TransactionInfo.CheckoutOption.Default
            ),
            merchantInfo = GooglePayJsonFactory.MerchantInfo(merchantName = config.companyName),
            billingAddressParameters = GooglePayJsonFactory.BillingAddressParameters(
                isRequired = false,
                format = if (config.addressFormat == Config.AddressFormat.Full) {
                    GooglePayJsonFactory.BillingAddressParameters.Format.Full
                } else {
                    GooglePayJsonFactory.BillingAddressParameters.Format.Min
                },
                isPhoneNumberRequired = config.phoneNumberRequired
            ),
            isEmailRequired = config.emailRequired,
            allowCreditCards = true
        )
    }
}