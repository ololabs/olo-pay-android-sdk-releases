// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import com.stripe.android.model.PaymentMethodCreateParams as StripePaymentMethodParams
import com.olo.olopay.data.IPaymentMethodParams

internal data class PaymentMethodParams internal constructor(internal val params: StripePaymentMethodParams) :
    IPaymentMethodParams {
}