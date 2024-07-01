// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.api

import android.content.Context
import com.olo.olopay.api.IOloPayApiInitializer.Companion.environment
import com.olo.olopay.api.IOloPayApiInitializer.Companion.googlePayConfig
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.data.SetupParameters
import com.olo.olopay.internal.data.Storage
import com.stripe.android.PaymentConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.olo.olopay.googlepay.GooglePayContext

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
     * Call this method prior to using [OloPayAPI] or [GooglePayContext]
     *
     * #### Important:
     * This method is provided mainly as convenience for Java developers. Kotlin developers should
     * generally use the suspend version of this method instead
     *
     * @param context The application context
     * @param parameters The parameters used for setting up the Olo Pay SDK
     * @param callback A callback to know when initialization is complete
     */
    override fun setup(context: Context, parameters: SetupParameters?, callback: InitCompleteCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            setup(context, parameters)
            callback.onComplete()
        }
    }

    /**
     * Call this method prior to using [OloPayAPI] or [GooglePayContext]
     * @param context The application context
     * @param parameters The parameters used for setting up the Olo Pay SDK
     */
    override suspend fun setup(context: Context, parameters: SetupParameters?) {
        try {

            googlePayConfig = parameters?.googlePayConfig
            environment = parameters?.environment ?: OloPayEnvironment.Production

            val publishableKey = Storage(context).getPublishableKey(environment)

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