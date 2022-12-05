// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.olopaysdk

import android.content.Context
import com.olo.olopay.api.IOloPayAPI
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.bootstrap.ApplicationProvider
import com.olo.olopay.api.IOloPayApiInitializer
import com.olo.olopay.api.OloPayApiInitializer
import com.olo.olopay.controls.PaymentCardDetailsForm
import com.olo.olopay.controls.PaymentCardDetailsMultiLineView
import com.olo.olopay.controls.PaymentCardDetailsSingleLineView
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.data.SetupParameters
import com.olo.olopay.googlepay.Config
import com.olo.olopay.googlepay.IGooglePayContext
import com.olo.olopay.googlepay.Environment
import com.olo.olopay.googlepay.Result
import com.olo.olopaytestharness.BuildConfig
import com.olo.olopaytestharness.R
import com.olo.olopaytestharness.oloapi.*
import com.olo.olopaytestharness.viewmodels.ActivityViewModel
import com.olo.olopaytestharness.viewmodels.SettingsViewModel
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
class KotlinSDKImplementation(
    override val viewModel: ActivityViewModel,
    override val settings: SettingsViewModel
) : SDKImplementation {
    private val apiClient: OloApiClient by lazy {
        createApiClientFromSettings(settings)
    }

    private var googlePayBasket: Basket? = null

    override val completePayment: Boolean
        get() = settings.completeOloPayPayment.value ?: false

    override fun submitPayment(cardDetails: PaymentCardDetailsSingleLineView) = blockingOperation {
        withContext(Dispatchers.Main) {
            viewModel.logText(viewModel.singleLineCardSubmitHeader)
            submitPayment(cardDetails.context, cardDetails.paymentMethodParams)
        }
    }

    override fun submitPayment(cardDetails: PaymentCardDetailsMultiLineView) = blockingOperation {
        withContext(Dispatchers.Main) {
            viewModel.logText(viewModel.multiLineCardSubmitHeader)
            submitPayment(cardDetails.context, cardDetails.paymentMethodParams)
        }
    }

    override fun submitPayment(cardDetails: PaymentCardDetailsForm) = blockingOperation {
        withContext(Dispatchers.Main) {
            viewModel.logText(viewModel.cardFormSubmitHeader)
            submitPayment(cardDetails.context, cardDetails.paymentMethodParams)
        }
    }

    override fun submitGooglePay(context: IGooglePayContext) = blockingOperation {
        googlePayBasket = null
        withContext(Dispatchers.Main) {
            viewModel.logText(viewModel.googlePaySubmitHeader)

            if (!completePayment) {
                context.present(amount = 12)
                return@withContext
            }

            try {
                withContext(Dispatchers.IO) {
                    googlePayBasket = createBasket()
                }

                context.present(amount = (googlePayBasket!!.total * 100).toInt())
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { viewModel.logException(e) }
            }
        }
    }

    override fun onGooglePayResult(result: Result) = blockingOperation {
        withContext(Dispatchers.Main) {
            viewModel.logGooglePayResult(result)

            if (!completePayment || result !is Result.Completed)
                return@withContext

            if (googlePayBasket == null) {
                viewModel.logText("Google Pay Basket not created")
                return@withContext
            }

            submitBasket(result.paymentMethod, googlePayBasket!!)
        }
    }

    private suspend fun submitPayment(context: Context, params: IPaymentMethodParams?) {
        withContext(Dispatchers.Main) { viewModel.logText("Card Is Valid: ${params != null}") }

        withContext(Dispatchers.IO) {
            try {
                val api: IOloPayAPI = OloPayAPI() //This could be mocked for testing purposes
                val paymentMethod = api.createPaymentMethod(context, params)

                withContext(Dispatchers.Main) { viewModel.logPaymentMethod(paymentMethod) }

                if (completePayment)
                    submitBasket(paymentMethod, createBasket())
            } catch (e: Exception) {
                // In a real application you would likely want to catch each exception type individually
                // and take appropriate action
                withContext(Dispatchers.Main) { viewModel.logException(e) }
            }
        }
    }

    private suspend fun createBasket(): Basket {
        return withContext(Dispatchers.IO) {
            val basket = apiClient.createBasketWithProductFromSettings(settings)
            withContext(Dispatchers.Main) { viewModel.logBasket(basket) }
            return@withContext basket
        }
    }

    private suspend fun submitBasket(paymentMethod: IPaymentMethod, basket: Basket) {
        try {
            val order = apiClient.submitBasketFromSettings(settings, paymentMethod, basket.id)
            withContext(Dispatchers.Main) { viewModel.logOrder(order) }
        } catch (e: Exception) {
            // In a real application you would likely want to catch each exception type individually
            // and take appropriate action
            withContext(Dispatchers.Main) { viewModel.logException(e) }
        }
    }

    private fun blockingOperation(operation: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) { viewModel.submissionInProgress.value = true }
            operation()
            withContext(Dispatchers.Main) { viewModel.submissionInProgress.value = false }
        }
    }

    companion object {
        // NOTE: This initialization should normally happen on app startup. For purposes of the test
        // harness app, where we want to test SDK initialization in both Java and Kotlin, we are
        // delaying it until launching the language-specific activity. See the SDK documentation about
        // initializing the SDK for more information about initializing on app startup
        val InitializeSDK by lazy {
            ApplicationProvider.listen { application ->
                val oloPayEnv = if (BuildConfig.DEBUG) OloPayEnvironment.Test else OloPayEnvironment.Production
                val freshInstall = application.resources.getBoolean(R.bool.fresh_install)

                val googlePayEnv = if (application.resources.getBoolean(R.bool.google_pay_production_env)) Environment.Production else Environment.Test
                val existingPaymentMethodsRequired = application.resources.getBoolean(R.bool.google_pay_existing_payment_methods_required)
                val googlePayConfig = Config(googlePayEnv, "Olo Pay SDK - Kotlin", existingPaymentMethodRequired = existingPaymentMethodsRequired)

                CoroutineScope(Dispatchers.IO).launch {
                    val initializer: IOloPayApiInitializer = OloPayApiInitializer()
                    initializer.setup(application.applicationContext, SetupParameters(oloPayEnv, freshInstall, googlePayConfig))
                }
            }
        }
    }
}