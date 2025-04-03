// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.olo.olopay.api.IOloPayApiInitializer
import com.olo.olopay.api.InitCompleteCallback
import com.olo.olopay.api.OloPayApiInitializer
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.internal.data.Storage
import com.olo.olopay.testhelpers.BooleanWrapper
import com.olo.olopay.testhelpers.waitForCondition
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class OloPayApiInitializerTests {
    @Test
    fun setup_environmentParameterNotSpecified_Production_isCached() {
        runBlocking {
            OloPayApiInitializer().setup(testContext)
            assertEquals(OloPayEnvironment.Production, IOloPayApiInitializer.environment)
        }
    }

    @Test
    fun setup_testPublishableKeyNull_testPublishableKeyUpdated() {
        Storage(testContext).testPublishableKey = null

        runBlocking {
            OloPayApiInitializer().setup(testContext, OloPayEnvironment.Test)
            assertNotNull(Storage(testContext).testPublishableKey)
        }
    }

    @Test
    fun setup_testPublishableKeyEmpty_testPublishableKeyUpdated() {
        Storage(testContext).testPublishableKey = ""

        runBlocking {
            OloPayApiInitializer().setup(testContext, OloPayEnvironment.Test)
            assertFalse(Storage(testContext).testPublishableKey.isNullOrEmpty())
        }
    }

    @Test
    fun setup_withCachedTestPublishableKey_testPublishableKeyNotChanged() {
        Storage(testContext).testPublishableKey = "Foobar"

        runBlocking {
            OloPayApiInitializer().setup(testContext, OloPayEnvironment.Test)
            assertEquals("Foobar", Storage(testContext).testPublishableKey)
        }
    }

    @Test
    fun setupWithCallback_callbackHandlerCalled() {
        val enteredCallback = BooleanWrapper(false)

        runBlocking {
            val callback = object: InitCompleteCallback {
                override fun onComplete() {
                    enteredCallback.value = true
                }
            }

            OloPayApiInitializer().setup(testContext, callback = callback)
            waitForCondition(enteredCallback)

            assertTrue(enteredCallback.value)
        }
    }

    private val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
}