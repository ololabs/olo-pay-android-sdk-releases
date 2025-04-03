// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.controls.PaymentCardDetailsForm
import com.olo.olopay.controls.PaymentCardDetailsMultiLineView
import com.olo.olopay.controls.PaymentCardDetailsSingleLineView

/**
 * Class used to create an [IPaymentMethod] instance from user input. Instances of this class can be retrieved
 * from [PaymentCardDetailsSingleLineView], [PaymentCardDetailsMultiLineView], and [PaymentCardDetailsForm]
 * @see OloPayAPI.createPaymentMethod
 */
interface IPaymentMethodParams {
}