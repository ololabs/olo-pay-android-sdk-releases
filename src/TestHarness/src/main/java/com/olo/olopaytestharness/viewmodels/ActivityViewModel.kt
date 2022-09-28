// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.viewmodels

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.googlepay.Result
import com.olo.olopaytestharness.R
import com.olo.olopaytestharness.oloapi.Basket
import com.olo.olopaytestharness.oloapi.Order
import java.lang.Exception

/*
    IMPORTANT: Anything related to the OloPaySDK belongs in either JavaSDKImplementation or KotlinSDKImplementation

    We need to make sure to not put any code in here that calls the Olo Pay SDK so that we can
    ensure the SDK works with Java. Since the SDK is written in Kotlin, there are little nuances
    that could affect SDK usage from an app written in Java (e.g @JvmStatic, @JvmOverloads, etc)
 */
class ActivityViewModel(application: Application) : AndroidViewModel(application) {
    val newSettingsHeader: String = "--------------- NEW SETTINGS ---------------"
    val singleLineCardSubmitHeader: String = "---------- SINGLE LINE CARD DETAILS SUBMISSION ----------"
    val multiLineCardSubmitHeader: String = "---------- MULTI LINE CARD DETAILS SUBMISSION ----------"
    val cardFormSubmitHeader: String = "------------ CARD FORM SUBMISSION ------------"
    val googlePaySubmitHeader: String = "----------- GOOGLE PAY SUBMISSION -----------"

    // IMPORTANT: ALL LOG-RELATED CALLS MUST BE CALLED FROM THE UI THREAD AND THAT IS THE RESPONSIBILITY OF THE CALLER
    //            Due to how LiveData works we cannot update this value by using postValue() or else we run the risk of losing
    //            logging output
    val logOutput: MutableLiveData<String> = MutableLiveData("")

    val submissionInProgress = MutableLiveData(false)

    val googlePayEnabled: LiveData<Boolean>
        get() = _googlePayEnabled

    val googlePayReady = MutableLiveData(false)

    private val _googlePayEnabled: MediatorLiveData<Boolean> = MediatorLiveData()

    init {
        _googlePayEnabled.addSource(submissionInProgress) { inProgress ->
            _googlePayEnabled.value = !inProgress && googlePayReady.value!!
        }

        _googlePayEnabled.addSource(googlePayReady) { ready ->
            _googlePayEnabled.value = ready && !submissionInProgress.value!!
        }
    }

    @Synchronized
    @MainThread
    @JvmOverloads
    fun logText(message: String?, prependNewLine: Boolean = true, appendNewLine: Boolean = true) {
        val currentLog = if (logOutput.value != null) logOutput.value!! else ""
        val newLog = StringBuilder(currentLog)

        if (prependNewLine)
            newLog.append("\n")

        if (message != null)
            newLog.append(message)

        if (appendNewLine)
            newLog.append("\n")

        logOutput.value = newLog.toString()
    }

    fun clearLog() {
        logOutput.value = ""
    }

    // Convenience method so we don't need to duplicate code for
    // both Java and Kotlin SDK integrations
    fun logSettings(settings: SettingsViewModel) {
        val app = settings.getApplication<Application>()
        val builder = StringBuilder()

        builder.appendLine(newSettingsHeader)

        if (settings.displayCardForm.value == true) {
            builder.appendLine("Payment Type: Card Form")
        } else {
            builder.appendLine("Payment Type: Card View")
            builder.appendLine("${app.getString(R.string.card_view_settings_display_single_line_card)}: ${settings.useSingleLineCardView.value}")
            builder.appendLine("${app.getString(R.string.card_view_settings_display_postal_code)}: ${settings.postalCodeEnabled.value}")
            builder.appendLine("${app.getString(R.string.card_view_settings_postal_code_required)}: ${settings.postalCodeRequired.value}")
            builder.appendLine("${app.getString(R.string.card_view_settings_us_postal_code_required)}: ${settings.usPostalCodeRequired.value}")
        }

        builder.appendLine("${app.getString(R.string.settings_log_input_changes)}: ${settings.logCardInputChanges.value}")

        builder.appendLine("${app.getString(R.string.settings_complete_olopay_payment)}: ${settings.completeOloPayPayment.value}")
        if (settings.completeOloPayPayment.value == true) {
            builder.appendLine("${app.getString(R.string.settings_api_url)}: ${settings.baseApiUrl.value}")
            builder.appendLine("${app.getString(R.string.settings_user_email)}: ${settings.userEmail.value}")
            builder.appendLine("${app.getString(R.string.settings_restaurant_id)}: ${settings.restaurantId.value}")
            builder.appendLine("${app.getString(R.string.settings_product_id)}: ${settings.productId.value}")
            builder.appendLine("${app.getString(R.string.settings_product_qty)}: ${settings.productQty.value}")
        }

        logText(builder.toString())
    }

    fun logBasket(basket: Basket?) {
        if (basket == null) {
            logText("Basket not created")
        } else {
            logText("Basket created...\n$basket")
        }
    }

    fun logOrder(order: Order?) {
        if (order == null) {
            logText("Order not created")
        } else {
            logText("Order created...\n$order")
        }
    }

    fun logPaymentMethod(paymentMethod: IPaymentMethod?) {
        if (paymentMethod == null) {
            logText("Payment method not created")
        } else {
            logText("Payment Method created...\n$paymentMethod")
        }
    }

    fun logException(e: Exception) {
        logText(e.toString())
    }

    fun logGooglePayResult(result: Result) {
        when (result) {
            is Result.Completed -> {
                logPaymentMethod(result.paymentMethod)
            }
            Result.Canceled -> {
                logText("Google Pay Canceled")
            }
            is Result.Failed -> {
                logException(result.error)
            }
        }
    }
}