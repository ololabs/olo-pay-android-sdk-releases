// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.olopaysdk

import com.olo.olopay.controls.PaymentCardDetailsForm
import com.olo.olopay.controls.PaymentCardDetailsMultiLineView
import com.olo.olopay.controls.PaymentCardDetailsSingleLineView
import com.olo.olopay.googlepay.GooglePayContext
import com.olo.olopay.googlepay.IGooglePayContext
import com.olo.olopay.googlepay.Result
import com.olo.olopaytestharness.viewmodels.ActivityViewModel
import com.olo.olopaytestharness.viewmodels.SettingsViewModel


interface SDKImplementation {
    val viewModel: ActivityViewModel
    val settings: SettingsViewModel
    val completePayment: Boolean

    fun submitPayment(cardDetails: PaymentCardDetailsSingleLineView)
    fun submitPayment(cardDetails: PaymentCardDetailsMultiLineView)
    fun submitPayment(cardDetails: PaymentCardDetailsForm)
    fun submitGooglePay(context: IGooglePayContext)
    fun onGooglePayResult(result: Result)
}