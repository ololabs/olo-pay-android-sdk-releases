// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.olopaysdk

import android.content.Context
import com.olo.olopay.data.ICvvTokenParams
import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopay.googlepay.GooglePayLineItem
import com.olo.olopay.googlepay.IGooglePayLauncher
import com.olo.olopay.googlepay.GooglePayResult
import com.olo.olopaytestharness.models.IGooglePaySettings
import com.olo.olopaytestharness.models.ILogger
import com.olo.olopaytestharness.models.IOloApiSettings
import com.olo.olopaytestharness.models.IUserSettings
import com.olo.olopaytestharness.models.IWorkerStatus
import com.olo.olopaytestharness.oloapi.entities.Basket

interface ISDKImplementation {
    val logger: ILogger
    val workerStatus: IWorkerStatus

    fun submitPayment(
        context: Context,
        params: IPaymentMethodParams?,
        oloApiSettings: IOloApiSettings,
        userSettings: IUserSettings
    )
    fun submitCvv(
        context: Context,
        params: ICvvTokenParams?,
        apiSettings: IOloApiSettings,
        userSettings: IUserSettings
    )
    fun submitGooglePay(
        context: IGooglePayLauncher,
        settings: IOloApiSettings,
        userSettings: IUserSettings,
        googlePaySettings: IGooglePaySettings,
        amount: Int,
        lineItems: List<GooglePayLineItem>?,
        basket: Basket?
    )
    fun onGooglePayResult(
        result: GooglePayResult,
        settings: IOloApiSettings,
        userSettings: IUserSettings
    )
}