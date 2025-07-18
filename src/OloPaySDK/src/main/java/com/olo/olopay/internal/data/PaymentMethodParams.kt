// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import com.stripe.android.model.PaymentMethodCreateParams as StripePaymentMethodParams
import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopay.googlepay.GooglePayConfig
import com.olo.olopay.internal.googlepay.GooglePaymentData

internal class PaymentMethodParams private constructor(
    private val stripeParams: StripePaymentMethodParams,
    private val metadataGenerator: MetadataGenerator,
    internal val googlePayData: GooglePaymentData? = null,
    internal val googlePayConfig: GooglePayConfig? = null
) : IPaymentMethodParams {

    internal constructor(
        stripeParams: StripePaymentMethodParams,
        source: PaymentMethodSource
    ) : this(
        stripeParams,
        MetadataGenerator(source)
    )

    internal constructor(
        stripeParams: StripePaymentMethodParams,
        config: GooglePayConfig,
        googlePayData: GooglePaymentData
    ) : this(
        stripeParams,
        MetadataGenerator(config),
        googlePayData,
        config
    )

    internal val params
        get() = StripePaymentMethodParams.create(stripeParams.card!!, stripeParams.billingDetails, metadataGenerator())
}