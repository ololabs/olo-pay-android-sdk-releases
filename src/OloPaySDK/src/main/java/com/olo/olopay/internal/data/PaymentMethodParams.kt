// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import com.stripe.android.model.PaymentMethodCreateParams as StripePaymentMethodParams
import com.olo.olopay.data.IPaymentMethodParams

internal class PaymentMethodParams internal constructor(
    private val stripeParams: StripePaymentMethodParams,
    source: PaymentMethodSource
) : IPaymentMethodParams {
    private val _metadataGenerator: MetadataGenerator = MetadataGenerator(source)

    internal val params
        get() = StripePaymentMethodParams.create(stripeParams.card!!, stripeParams.billingDetails, _metadataGenerator())
}