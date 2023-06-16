// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.googlepay.GooglePayErrorType
import com.stripe.android.googlepaylauncher.GooglePayPaymentMethodLauncher
import org.junit.Assert.*
import org.junit.Test

class GooglePayErrorTypeTests {
    @Test
    fun from_stripeErrorCode_To_OloPayType_DeveloperError() {
        assertEquals(GooglePayErrorType.DeveloperError, GooglePayErrorType.from(GooglePayPaymentMethodLauncher.DEVELOPER_ERROR))
    }

    @Test
    fun from_stripeErrorCode_To_OloPayType_NetworkError() {
        assertEquals(GooglePayErrorType.NetworkError, GooglePayErrorType.from(GooglePayPaymentMethodLauncher.NETWORK_ERROR))
    }

    @Test
    fun from_stripeErrorCode_To_OloPayType_InternalError() {
        assertEquals(GooglePayErrorType.InternalError, GooglePayErrorType.from(GooglePayPaymentMethodLauncher.INTERNAL_ERROR))
    }

    @Test
    fun from_unknownStripeErrorCode_To_OloPayType_InternalError() {
        assertEquals(GooglePayErrorType.InternalError, GooglePayErrorType.from(30))
    }
}