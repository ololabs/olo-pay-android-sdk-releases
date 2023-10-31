// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.olopaysdk

import android.content.Context
import com.olo.olopay.api.IOloPayAPI
import com.olo.olopay.api.IOloPayApiInitializer
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.api.OloPayApiInitializer
import com.olo.olopay.data.ICvvTokenParams
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.data.SetupParameters
import com.olo.olopay.googlepay.Config
import com.olo.olopay.googlepay.Environment
import com.olo.olopay.googlepay.IGooglePayContext
import com.olo.olopay.googlepay.Result
import com.olo.olopaytestharness.BuildConfig
import com.olo.olopaytestharness.R
import com.olo.olopaytestharness.oloapi.Basket
import com.olo.olopaytestharness.oloapi.OloApiClient
import com.olo.olopaytestharness.oloapi.createApiClientFromSettings
import com.olo.olopaytestharness.oloapi.createBasketWithProductFromSettings
import com.olo.olopaytestharness.oloapi.submitBasketFromSettings
import com.olo.olopaytestharness.models.ILogger
import com.olo.olopaytestharness.models.IOloApiSettings
import com.olo.olopaytestharness.models.IWorkerStatus
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
    override fun submitPayment(context: Context, params: IPaymentMethodParams?, oloApiSettings: IOloApiSettings) = backgroundOperation {
        val apiClient = createApiClientFromSettings(oloApiSettings)

        withContext(Dispatchers.Main) { logger.logText("Card Is Valid: ${params != null}")}
        withContext(Dispatchers.IO) {
            try {
                val paymentMethod = api.createPaymentMethod(context, params)

                withContext(Dispatchers.Main) { logger.logPaymentMethod(paymentMethod) }

                if (oloApiSettings.completePayment.value) {
                    val basket = createBasket(apiClient, oloApiSettings)
                    submitBasket(apiClient, paymentMethod, basket, oloApiSettings)
                }
            } catch (e: Exception) {
                // In a real application you would likely want to catch each exception type individually
                // and take appropriate action
                withContext(Dispatchers.Main) { logger.logException(e) }
            }
        }
    }

    override fun submitCvv(context: Context, params: ICvvTokenParams?) = backgroundOperation {
        try {
            if(params == null){
                withContext(Dispatchers.Main) { logger.logText("CVV Params not valid") }
                return@backgroundOperation
            }

            val cvvUpdateToken = api.createCvvUpdateToken(context, params)
            withContext(Dispatchers.Main) {
                // In a production application you would use this token with the Olo Ordering API
                // to revalidate a saved card
                logger.logCvvToken(cvvUpdateToken)
            }

        } catch (e: Exception) {
            // In a production application you would likely want to catch each exception type individually
            // and take appropriate action
            withContext(Dispatchers.Main) { logger.logException(e) }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun submitGooglePay(context: IGooglePayContext, settings: IOloApiSettings) = backgroundOperation {
        googlePayBasket = null
        val apiClient = createApiClientFromSettings(settings)

        withContext(Dispatchers.Main) {
            logger.logText(GooglePaySubmitHeader)

            if (!settings.completePayment.value) {
                context.present(amount = 12)
                return@withContext
            }

            try {
                withContext(Dispatchers.IO) {
                    googlePayBasket = createBasket(apiClient, settings)
                }

                context.present(amount = (googlePayBasket!!.total * 100).toInt())
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { logger.logException(e) }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun onGooglePayResult(result: Result, settings: IOloApiSettings) = backgroundOperation {
        val apiClient = createApiClientFromSettings(settings)

        withContext(Dispatchers.Main) {
            logger.logGooglePayResult(result)

            if (!settings.completePayment.value || result !is Result.Completed)
                return@withContext

            if (googlePayBasket == null) {
                logger.logText("Google Pay Basket not created")
                return@withContext
            }

            submitBasket(apiClient, result.paymentMethod, googlePayBasket!!, settings)
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
    private suspend fun submitBasket(apiClient: OloApiClient, paymentMethod: IPaymentMethod, basket: Basket, settings: IOloApiSettings) {
        try {
            val order = apiClient.submitBasketFromSettings(settings, paymentMethod, basket.id)
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
                val oloPayEnv = if (BuildConfig.DEBUG) OloPayEnvironment.Test else OloPayEnvironment.Production
                val freshInstall = appContext.resources.getBoolean(R.bool.fresh_install)

                val googlePayEnv = if (appContext.resources.getBoolean(R.bool.google_pay_production_env)) Environment.Production else Environment.Test
                val existingPaymentMethodsRequired = appContext.resources.getBoolean(R.bool.google_pay_existing_payment_methods_required)
                val googlePayConfig = Config(googlePayEnv, "Olo Pay SDK - Kotlin", existingPaymentMethodRequired = existingPaymentMethodsRequired)

                val initializer: IOloPayApiInitializer = OloPayApiInitializer()
                initializer.setup(appContext, SetupParameters(oloPayEnv, freshInstall, googlePayConfig))
            }
        }

        const val GooglePaySubmitHeader: String = "----------- GOOGLE PAY SUBMISSION -----------"
    }
}