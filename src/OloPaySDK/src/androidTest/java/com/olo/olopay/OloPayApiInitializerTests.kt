// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.olo.olopay.api.IOloPayApiInitializer
import com.olo.olopay.api.InitCompleteCallback
import com.olo.olopay.api.OloPayApiInitializer
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.data.SetupParameters
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
            OloPayApiInitializer().setup(testContext, SetupParameters())
            assertEquals(OloPayEnvironment.Production, IOloPayApiInitializer.environment)
        }
    }

    @Test
    fun setup_withFreshSetup_publishableKeyWithValue_publishableKeyUpdated() {
        Storage(testContext).publishableKey = "Foobar"

        runBlocking {
            OloPayApiInitializer().setup(testContext, SetupParameters(freshSetup = true))
            assertNotEquals("Foobar", Storage(testContext).publishableKey)
        }
    }

    @Test
    fun setup_withFreshSetup_publishableKeyNull_publishableKeyUpdated() {
        Storage(testContext).publishableKey = null

        runBlocking {
            OloPayApiInitializer().setup(testContext, SetupParameters(freshSetup = true))
            assertNotNull(Storage(testContext).publishableKey)
        }
    }

    @Test
    fun setup_withFreshSetup_publishableKeyEmpty_publishableKeyUpdated() {
        Storage(testContext).publishableKey = ""
        runBlocking {
            OloPayApiInitializer().setup(testContext, SetupParameters(freshSetup = true))
            assertFalse(Storage(testContext).publishableKey.isNullOrEmpty())
        }
    }

    @Test
    fun setup_withoutFreshSetup_withCachedPublishableKey_publishableKeyNotChanged() {
        Storage(testContext).publishableKey = "foobar"
        runBlocking {
            OloPayApiInitializer().setup(testContext, SetupParameters(freshSetup = false))
            assertEquals("foobar", Storage(testContext).publishableKey)
        }
    }

    @Test
    fun setup_withoutFreshSetup_publishableKeyNull_publishableKeyUpdated() {
        Storage(testContext).publishableKey = null
        runBlocking {
            OloPayApiInitializer().setup(testContext, SetupParameters(freshSetup = false))
            assertFalse(Storage(testContext).publishableKey.isNullOrEmpty())
        }
    }

    @Test
    fun setup_withoutFreshSetup_publishableKeyEmpty_publishableKeyUpdated() {
        Storage(testContext).publishableKey = ""

        runBlocking {
            OloPayApiInitializer().setup(testContext, SetupParameters(freshSetup = false))
            assertFalse(Storage(testContext).publishableKey.isNullOrEmpty())
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