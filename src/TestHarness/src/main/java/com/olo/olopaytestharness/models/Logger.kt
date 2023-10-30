// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import android.content.Context
import com.olo.olopay.data.ICvvUpdateToken
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.googlepay.Result
import com.olo.olopaytestharness.R
import com.olo.olopaytestharness.oloapi.Basket
import com.olo.olopaytestharness.oloapi.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.Exception

class Logger : ILogger {
    private val _logOutput = MutableStateFlow("")
    override val logOutput: Flow<String>
        get() = _logOutput

    override fun logText(message: String?, prependNewLine: Boolean, appendNewLine: Boolean) {
        val currentLog = _logOutput.value
        val newLog = StringBuilder(currentLog)

        if (prependNewLine)
            newLog.append("\n")

        if (message != null)
            newLog.append(message)

        if (appendNewLine)
            newLog.append("\n")

        _logOutput.value = newLog.toString()
    }

    override fun logText(message: String?, prependNewLine: Boolean) {
        logText(message, prependNewLine, true)
    }

    override fun logText(message: String?) {
        logText(message, prependNewLine = true, appendNewLine = true)
    }

    override fun logPaymentMethod(paymentMethod: IPaymentMethod?) {
        if (paymentMethod == null) {
            logText("Payment method not created")
        } else {
            logText("Payment Method created...\n$paymentMethod")
        }
    }

    override fun logCvvToken(cvvUpdateToken: ICvvUpdateToken) {
        logText("CVV Update Token created...\n$cvvUpdateToken")
    }

    override fun logException(e: Exception) {
        logText(e.toString())
    }

    override fun logGooglePayResult(result: Result) {
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

    override fun logBasket(basket: Basket?) {
        if (basket == null) {
            logText("Basket not created")
        } else {
            logText("Basket created...\n$basket")
        }
    }

    override fun logOrder(order: Order?) {
        if (order == null) {
            logText("Order not created")
        } else {
            logText("Order created...\n$order")
        }
    }

    override fun logOloApiSettings(settings: IOloApiSettings, context: Context) {
        val appContext = context.applicationContext
        val builder = StringBuilder()

        builder.appendLine(OloApiSettingsHeader)
        builder.appendLine("${appContext.getString(R.string.settings_complete_olopay_payment)}: ${settings.completePayment.value}")
        if (settings.completePayment.value) {
            builder.appendLine("${appContext.getString(R.string.settings_api_url)}: ${settings.baseApiUrl.value}")
            builder.appendLine("${appContext.getString(R.string.settings_user_email)}: ${settings.userEmail.value}")
            builder.appendLine("${appContext.getString(R.string.settings_restaurant_id)}: ${settings.restaurantId.value}")
            builder.appendLine("${appContext.getString(R.string.settings_product_id)}: ${settings.productId.value}")
            builder.appendLine("${appContext.getString(R.string.settings_product_qty)}: ${settings.productQty.value}")
        }

        logText(builder.toString())
    }

    override fun logCardSettings(settings: ICardSettings, context: Context) {
        val appContext = context.applicationContext
        val builder = StringBuilder()

        builder.appendLine(CardUISettingsHeader)

        if (settings.displayCardForm.value) {
            builder.appendLine("Payment Type: Card Form")
        } else {
            builder.appendLine("Payment Type: Card View")
            builder.appendLine("${appContext.getString(R.string.card_view_settings_display_single_line_card)}: ${settings.useSingleLineCardView.value}")
            builder.appendLine("${appContext.getString(R.string.card_view_settings_display_postal_code)}: ${settings.postalCodeEnabled.value}")
        }

        builder.appendLine("${appContext.getString(R.string.settings_log_input_changes)}: ${settings.logCardInputChanges.value}")

        logText(builder.toString())
    }

    override fun logCvvSettings(settings: ICvvSettings, context: Context) {
        val appContext = context.applicationContext
        val builder = StringBuilder()

        builder.appendLine(CvvUISettingsHeader)
        builder.appendLine("${appContext.getString(R.string.cvv_view_settings_display_errors)}: ${settings.displayCvvErrors.value}")
        builder.appendLine("${appContext.getString(R.string.settings_log_input_changes)}: ${settings.logCvvInputChanges.value}")

        logText(builder.toString())
    }

    override fun clearLog() {
        _logOutput.value = ""
    }

    companion object {
        private const val OloApiSettingsHeader: String = "---------- OLO API SETTINGS ----------"
        private const val CardUISettingsHeader: String = "---------- CARD UI SETTINGS ----------"
        private const val CvvUISettingsHeader: String = "----------- CVV UI SETTINGS -----------"
    }
}
