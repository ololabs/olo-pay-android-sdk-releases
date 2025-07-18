// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.api

import android.content.Context
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.internal.data.Storage
import com.stripe.android.PaymentConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.olo.olopay.googlepay.GooglePayLauncher

/**
 * Class to set up and initialize the Olo Pay API
 *
 * @see com.olo.olopay.bootstrap.ApplicationInitializer Convenience class for initializing the Olo Pay SDK
 * @see com.olo.olopay.bootstrap.ApplicationProvider Convenience class for initializing the Olo Pay SDK
 *
 * @constructor Creates a new instance of [OloPayApiInitializer]
 */
class OloPayApiInitializer : IOloPayApiInitializer {
    /**
     * Call this method prior to using [OloPayAPI] or [GooglePayLauncher]
     *
     * #### Important:
     * This method is provided mainly as convenience for Java developers. Kotlin developers should
     * generally use the suspend version of this method instead
     *
     * @param context The application context
     * @param environment The environment to use when setting up the SDK
     * @param callback A callback to know when initialization is complete
     */
    override fun setup(context: Context, environment: OloPayEnvironment, callback: InitCompleteCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            setup(context, environment)
            callback.onComplete()
        }
    }

    /**
     * Call this method prior to using [OloPayAPI] or [GooglePayLauncher]
     * @param context The application context
     * @param environment The environment to use when setting up the SDK
     */
    override suspend fun setup(context: Context, environment: OloPayEnvironment) {
        try {
            val publishableKey = Storage(context).getPublishableKey(environment)
            IOloPayApiInitializer.environment = environment

            if (publishableKey.isNullOrEmpty()) {
                OloPayAPI.updatePublishableKey(context)
            } else {
                PaymentConfiguration.init(context, publishableKey)
            }
        } catch (e: Exception) {
            // Swallow this... if setting the publishable key fails on init, the SDK will attempt
            // to set it when creating a payment method, at which point an appropriate error
            // will be generated
        }

    }
}