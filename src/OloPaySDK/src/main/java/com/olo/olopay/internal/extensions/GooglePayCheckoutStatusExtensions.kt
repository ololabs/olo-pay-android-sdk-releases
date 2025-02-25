// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.extensions

import com.olo.olopay.googlepay.GooglePayCheckoutStatus
import com.stripe.android.GooglePayJsonFactory.TransactionInfo.CheckoutOption as StripeCheckoutOption
import com.stripe.android.GooglePayJsonFactory.TransactionInfo.TotalPriceStatus as StripePriceStatus

fun GooglePayCheckoutStatus.toStripeCheckoutOption() : StripeCheckoutOption {
    return when (this) {
        GooglePayCheckoutStatus.EstimatedDefault -> StripeCheckoutOption.Default
        GooglePayCheckoutStatus.FinalDefault -> StripeCheckoutOption.Default
        GooglePayCheckoutStatus.FinalImmediatePurchase -> StripeCheckoutOption.CompleteImmediatePurchase
    }
}

fun GooglePayCheckoutStatus.toStripePriceStatus() : StripePriceStatus {
    return when (this) {
        GooglePayCheckoutStatus.EstimatedDefault -> StripePriceStatus.Estimated
        GooglePayCheckoutStatus.FinalDefault -> StripePriceStatus.Final
        GooglePayCheckoutStatus.FinalImmediatePurchase -> StripePriceStatus.Final
    }
}