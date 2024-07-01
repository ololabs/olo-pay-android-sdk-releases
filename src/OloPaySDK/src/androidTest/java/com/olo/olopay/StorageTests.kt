// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.internal.data.Storage
import org.junit.Assert.*
import org.junit.Test

class StorageTests {

    @Test
    fun getPublishableKey_withTestEnvironment_returnsTestKey() {
        Storage(testContext).testPublishableKey = "Test"

        assertEquals(
            "Test",
            Storage(testContext).getPublishableKey(OloPayEnvironment.Test)
        )
    }

    @Test
    fun setPublishableKey_withTestEnvironment_setsOnlyTestKey() {
        // Teardown
        Storage(testContext).testPublishableKey = null
        Storage(testContext).productionPublishableKey = null

        Storage(testContext).setPublishableKey(OloPayEnvironment.Test, "Test")
        assertEquals("Test", Storage(testContext).testPublishableKey)
        assertNull(Storage(testContext).productionPublishableKey)
    }

    @Test
    fun getPublishableKey_withProductionEnvironment_returnsProductionKey() {
        Storage(testContext).productionPublishableKey = "Production"

        assertEquals(
            "Production",
            Storage(testContext).getPublishableKey(OloPayEnvironment.Production)
        )
    }

    @Test
    fun setPublishableKey_withProductionEnvironment_setsOnlyProductionKey() {
        // Teardown
        Storage(testContext).productionPublishableKey = null
        Storage(testContext).testPublishableKey = null

        Storage(testContext).setPublishableKey(OloPayEnvironment.Production, "Production")
        assertEquals("Production", Storage(testContext).productionPublishableKey)
        assertNull(Storage(testContext).testPublishableKey)
    }

    private val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
}