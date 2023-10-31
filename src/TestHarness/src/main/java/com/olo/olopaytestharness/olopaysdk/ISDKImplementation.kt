// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.olopaysdk

import android.content.Context
import com.olo.olopay.data.ICvvTokenParams
import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopay.googlepay.IGooglePayContext
import com.olo.olopay.googlepay.Result
import com.olo.olopaytestharness.models.ILogger
import com.olo.olopaytestharness.models.IOloApiSettings
import com.olo.olopaytestharness.models.IWorkerStatus

interface ISDKImplementation {
    val logger: ILogger
    val workerStatus: IWorkerStatus

    fun submitPayment(context: Context, params: IPaymentMethodParams?, oloApiSettings: IOloApiSettings)
    fun submitCvv(context: Context, params: ICvvTokenParams?)
    fun submitGooglePay(context: IGooglePayContext, settings: IOloApiSettings)
    fun onGooglePayResult(result: Result, settings: IOloApiSettings)
}