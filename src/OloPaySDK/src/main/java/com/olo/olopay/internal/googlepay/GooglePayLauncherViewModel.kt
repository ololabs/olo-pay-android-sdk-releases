// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.googlepay

import android.content.Context
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.exceptions.ApiConnectionException
import com.olo.olopay.googlepay.GooglePayException
import com.olo.olopay.exceptions.InvalidRequestException
import com.olo.olopay.googlepay.GooglePayErrorType
import com.olo.olopay.googlepay.GooglePayResult
import com.olo.olopay.internal.data.PaymentMethodParams
import com.stripe.android.model.PaymentMethodCreateParams
import org.json.JSONObject

internal class GooglePayLauncherViewModel constructor(
    private val launcherArgs: GooglePayLauncherArgs,
    private val savedStateHandle: SavedStateHandle,
    private val googlePayRepository: GooglePayRepository
): ViewModel() {
    private val _googleResult = MutableLiveData<GooglePayResult>()

    internal var hasLaunched: Boolean
        get() = savedStateHandle.get<Boolean>(HAS_LAUNCHED_KEY) == true
        set(value) = savedStateHandle.set(HAS_LAUNCHED_KEY, value)

    internal val googlePayResult = _googleResult.distinctUntilChanged()

    internal fun updateResult(result: GooglePayResult) {
        _googleResult.value = result
    }

    internal fun createPaymentDataTask(): Task<PaymentData> {
      return googlePayRepository.createPaymentDataTask(
            launcherArgs.transactionId,
            launcherArgs.amount,
            launcherArgs.checkoutStatus,
            launcherArgs.totalPriceLabel,
            launcherArgs.lineItems
          )
    }

    internal suspend fun createPaymentMethod(context: Context, paymentData: PaymentData): GooglePayResult {
        val paymentDataJson = JSONObject(paymentData.toJson())
        val params = PaymentMethodParams(
            PaymentMethodCreateParams.createFromGooglePay(paymentDataJson),
            googlePayRepository.config,
            GooglePaymentData.fromJson(paymentDataJson)
        )

        return runCatching {
            OloPayAPI().createPaymentMethod(context, params)
        }.fold(
            onSuccess = { paymentMethod ->
                GooglePayResult.Completed(paymentMethod)
            },
            onFailure = { error ->
                val errorType = when(error) {
                    is ApiConnectionException -> GooglePayErrorType.NetworkError
                    is InvalidRequestException -> GooglePayErrorType.DeveloperError
                    else -> GooglePayErrorType.InternalError
                }

                GooglePayResult.Failed(GooglePayException(error, errorType))
            }
        )
    }

    private companion object {
        private const val HAS_LAUNCHED_KEY = "GooglePayLauncherHasLaunched"
    }

    @Suppress("UNCHECKED_CAST")
    internal class Factory(
        private val args: GooglePayLauncherArgs,
        private val repository: GooglePayRepository
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val savedStateHandle = extras.createSavedStateHandle()
            return GooglePayLauncherViewModel(args, savedStateHandle, repository) as T
        }
    }
}