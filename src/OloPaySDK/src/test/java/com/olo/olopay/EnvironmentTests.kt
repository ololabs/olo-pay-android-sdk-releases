// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.googlepay.Environment
import com.stripe.android.googlepaylauncher.GooglePayEnvironment
import org.junit.Assert.*
import org.junit.Test

class EnvironmentTests {
    @Test
    fun convertFrom_OloPayGooglePayEnvironment_To_StripeGooglePayEnvironment_Test() {
        assertEquals(GooglePayEnvironment.Test, Environment.convertFrom(Environment.Test))
    }

    @Test
    fun convertFrom_OloPayGooglePayEnvironment_To_StripeGooglePayEnvironment_Production() {
        assertEquals(GooglePayEnvironment.Production, Environment.convertFrom(Environment.Production))
    }
}