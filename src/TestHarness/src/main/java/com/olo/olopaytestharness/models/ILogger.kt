// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import android.content.Context
import com.olo.olopay.data.ICvvUpdateToken
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.googlepay.Result
import com.olo.olopaytestharness.oloapi.Basket
import com.olo.olopaytestharness.oloapi.Order
import kotlinx.coroutines.flow.Flow
import java.lang.Exception

interface ILogger {
    val logOutput: Flow<String>

    fun logText(message: String?, prependNewLine: Boolean, appendNewLine: Boolean)
    fun logText(message: String?, prependNewLine: Boolean)
    fun logText(message: String?)
    fun logPaymentMethod(paymentMethod: IPaymentMethod?)
    fun logCvvToken(cvvUpdateToken: ICvvUpdateToken)
    fun logException(e: Exception)
    fun logGooglePayResult(result: Result)
    fun logBasket(basket: Basket?)
    fun logOrder(order: Order?)
    fun logOloApiSettings(settings: IOloApiSettings, context: Context)
    fun logCardSettings(settings: ICardSettings, context: Context)
    fun logCvvSettings(settings: ICvvSettings, context: Context)
    fun clearLog()
}
