// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.api

import android.content.Context
import com.olo.olopay.data.ICvvUpdateToken
import com.olo.olopay.data.ICvvTokenParams
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopay.data.IPaymentMethodParams

/** Interface for mocking/testing purposes */
interface IOloPayAPI {
    /** Create a payment method. See [OloPayAPI] for method documentation */
    fun createPaymentMethod(context: Context, params: IPaymentMethodParams?, callback: ApiResultCallback<IPaymentMethod?>)

    /** Create a payment method. See [OloPayAPI] for method documentation */
    suspend fun createPaymentMethod(context: Context, params: IPaymentMethodParams?): IPaymentMethod

    /** Create a CVV validation token. See [OloPayAPI] for method documentation */
    fun createCvvUpdateToken(context: Context, params: ICvvTokenParams, callback: ApiResultCallback<ICvvUpdateToken?>)

    /** Create a CVV validation token. See [OloPayAPI] for method documentation */
    suspend fun createCvvUpdateToken(context: Context, params: ICvvTokenParams): ICvvUpdateToken

}