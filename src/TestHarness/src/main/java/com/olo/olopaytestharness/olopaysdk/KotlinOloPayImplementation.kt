// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.olopaysdk

import android.content.Context
import com.olo.olopay.api.IOloPayAPI
import com.olo.olopay.api.IOloPayApiInitializer
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.api.OloPayApiInitializer
import com.olo.olopay.data.CurrencyCode
import com.olo.olopay.data.ICvvTokenParams
import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.googlepay.GooglePayCheckoutStatus
import com.olo.olopay.googlepay.GooglePayLineItem
import com.olo.olopay.googlepay.IGooglePayLauncher
import com.olo.olopay.googlepay.GooglePayResult
import com.olo.olopaytestharness.BuildConfig
import com.olo.olopaytestharness.models.IGooglePaySettings
import com.olo.olopaytestharness.oloapi.entities.Basket
import com.olo.olopaytestharness.oloapi.OloApiClient
import com.olo.olopaytestharness.oloapi.createApiClientFromSettings
import com.olo.olopaytestharness.oloapi.createBasketWithProductFromSettings
import com.olo.olopaytestharness.oloapi.submitBasketFromSettings
import com.olo.olopaytestharness.models.ILogger
import com.olo.olopaytestharness.models.IOloApiSettings
import com.olo.olopaytestharness.models.IUserSettings
import com.olo.olopaytestharness.models.IWorkerStatus
import com.olo.olopaytestharness.oloapi.entities.PaymentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi

class KotlinOloPayImplementation(
    override val logger: ILogger,
    override val workerStatus: IWorkerStatus
) : ISDKImplementation {

    // In a production app this could be injected via DI and mocked for testing purposes
    private val api: IOloPayAPI = OloPayAPI()

    private var googlePayBasket: Basket? = null

    @OptIn(ExperimentalSerializationApi::class)
    override fun submitPayment(context: Context, params: IPaymentMethodParams?, oloApiSettings: IOloApiSettings, userSettings: IUserSettings) = backgroundOperation {
        val apiClient = createApiClientFromSettings(oloApiSettings)

        withContext(Dispatchers.Main) { logger.logText("Card Is Valid: ${params != null}")}

        try {
            val paymentType = PaymentType(paymentMethod = api.createPaymentMethod(context, params))

            withContext(Dispatchers.Main) { logger.logPaymentMethod(paymentType.paymentMethod) }

            if (oloApiSettings.completePayment.value) {
                val basket = createBasket(apiClient, oloApiSettings)
                submitBasket(apiClient, paymentType, basket, oloApiSettings, userSettings)
            }
        } catch (e: Exception) {
            // In a real application you would likely want to catch each exception type individually
            // and take appropriate action
            withContext(Dispatchers.Main) { logger.logException(e) }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun submitCvv(context: Context, params: ICvvTokenParams?, oloApiSettings: IOloApiSettings, userSettings: IUserSettings) = backgroundOperation {
        val apiClient = createApiClientFromSettings(oloApiSettings)

        if(params == null){
            withContext(Dispatchers.Main) { logger.logText("CVV Params not valid") }
            return@backgroundOperation
        }

        try {
            val paymentType = PaymentType(token = api.createCvvUpdateToken(context, params))

            // In a production application you would use this token with the Olo Ordering API
            // to revalidate a saved card
            withContext(Dispatchers.Main) { logger.logCvvToken(paymentType.cvvToken!!) }

            if (oloApiSettings.completePayment.value) {
                val basket = createBasket(apiClient, oloApiSettings)
                submitBasket(apiClient, paymentType, basket, oloApiSettings, userSettings)
            }
        } catch (e: Exception) {
            // In a production application you would likely want to catch each exception type individually
            // and take appropriate action
            withContext(Dispatchers.Main) { logger.logException(e) }
        }
    }

    override fun submitGooglePay(
        launcher: IGooglePayLauncher,
        apiSettings: IOloApiSettings,
        userSettings: IUserSettings,
        googlePaySettings: IGooglePaySettings,
        amount: Int,
        lineItems: List<GooglePayLineItem>?,
        basket: Basket?
    ) = backgroundOperation {
        googlePayBasket = basket
        val checkoutStatus = if (googlePaySettings.usePayNowGooglePayButton.value) GooglePayCheckoutStatus.FinalImmediatePurchase else GooglePayCheckoutStatus.EstimatedDefault

        withContext(Dispatchers.Main) {
            logger.logText(GooglePaySubmitHeader)
            try {
                launcher.present(
                    amount,
                    checkoutStatus,
                    null,
                    lineItems
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { logger.logException(e) }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun onGooglePayResult(result: GooglePayResult, apiSettings: IOloApiSettings, userSettings: IUserSettings) = backgroundOperation {
        val apiClient = createApiClientFromSettings(apiSettings)

        withContext(Dispatchers.Main) {
            logger.logGooglePayResult(result)

            if (!apiSettings.completePayment.value || result !is GooglePayResult.Completed)
                return@withContext

            if (googlePayBasket == null) {
                logger.logText("Google Pay Basket not created")
                return@withContext
            }

            val paymentType = PaymentType(paymentMethod = result.paymentMethod)

            submitBasket(apiClient, paymentType, googlePayBasket!!, apiSettings, userSettings)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun createBasket(apiClient: OloApiClient, settings: IOloApiSettings): Basket {
        return withContext(Dispatchers.IO) {
            val basket = apiClient.createBasketWithProductFromSettings(settings)
            withContext(Dispatchers.Main) { logger.logBasket(basket) }
            return@withContext basket
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun submitBasket(apiClient: OloApiClient, paymentType: PaymentType, basket: Basket, apiSettings: IOloApiSettings, userSettings: IUserSettings) {
        try {
            val order = apiClient.submitBasketFromSettings(apiSettings, userSettings, paymentType, basket.id)

            withContext(Dispatchers.Main) { logger.logOrder(order) }
        } catch (e: Exception) {
            // In a real application you would likely want to catch each exception type individually
            // and take appropriate action
            withContext(Dispatchers.Main) { logger.logException(e) }
        }
    }

    private fun backgroundOperation(operation: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) { workerStatus.isBusy.value = true }
            operation()
            withContext(Dispatchers.Main) { workerStatus.isBusy.value = false }
        }
    }

    companion object {
        suspend fun initializeSdk(context: Context) {
            val appContext = context.applicationContext
            withContext(Dispatchers.IO) {
                //In a production app this could be mocked for testing purposes
                val initializer: IOloPayApiInitializer = OloPayApiInitializer()

                val oloPayEnv = if (BuildConfig.DEBUG) OloPayEnvironment.Test else OloPayEnvironment.Production
                initializer.setup(appContext, oloPayEnv)
            }
        }

        const val GooglePaySubmitHeader: String = "----------- GOOGLE PAY SUBMISSION -----------"
    }
}