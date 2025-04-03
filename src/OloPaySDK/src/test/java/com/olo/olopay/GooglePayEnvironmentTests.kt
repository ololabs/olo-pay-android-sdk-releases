// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.googlepay.GooglePayEnvironment
import com.stripe.android.googlepaylauncher.GooglePayEnvironment as StripeGooglePayEnvironment
import org.junit.Assert.*
import org.junit.Test

class GooglePayEnvironmentTests {
    @Test
    fun convertFrom_OloPayGooglePayEnvironment_To_StripeGooglePayEnvironment_Test() {
        assertEquals(StripeGooglePayEnvironment.Test, GooglePayEnvironment.convertFrom(GooglePayEnvironment.Test))
    }

    @Test
    fun convertFrom_OloPayGooglePayEnvironment_To_StripeGooglePayEnvironment_Production() {
        assertEquals(StripeGooglePayEnvironment.Production, GooglePayEnvironment.convertFrom(GooglePayEnvironment.Production))
    }
}