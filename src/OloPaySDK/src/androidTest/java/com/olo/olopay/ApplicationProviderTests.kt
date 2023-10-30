// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import android.app.Application
import androidx.test.platform.app.InstrumentationRegistry
import com.olo.olopay.bootstrap.ApplicationProvider
import com.olo.olopay.bootstrap.appInstance
import com.olo.olopay.testhelpers.BooleanWrapper
import com.olo.olopay.testhelpers.waitForCondition
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ApplicationProviderTests {
    @Before
    fun setup() {
        ApplicationProvider.applicationListeners.clear()
        appInstance = null
    }

    @Test
    fun listen_applicationNull_addedToApplicationListeners() {
        ApplicationProvider.listen {
            //Do nothing, it won't be called
        }

        assertTrue(ApplicationProvider.applicationListeners.isNotEmpty())
    }

    @Test
    fun listen_applicationNotNull_listenCalled() {
        val listenCalled = BooleanWrapper(false)
        appInstance = testApplication
        ApplicationProvider.listen {
            listenCalled.value = true
        }

        assertTrue(ApplicationProvider.applicationListeners.isEmpty())
        assertTrue(listenCalled.value)
    }

    @Test
    fun currentApplication_appInstanceNull_setsAndReturnsNonNullAppInstance() {
        val app = ApplicationProvider.currentApplication
        assertNotNull(app)
        assertEquals(app, appInstance)
    }

    @Test
    fun appInstance_whenSet_callsApplicationListeners() {
        val listenCalled = BooleanWrapper(false)
        ApplicationProvider.listen {
            listenCalled.value = true
        }

        appInstance = testApplication

        waitForCondition(listenCalled)
        assertFalse(ApplicationProvider.applicationListeners.isEmpty())
        assertTrue(listenCalled.value)
    }

    private val testApplication: Application
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
}