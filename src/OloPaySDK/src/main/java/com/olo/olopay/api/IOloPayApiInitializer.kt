// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.api

import android.content.Context
import com.olo.olopay.R
import com.olo.olopay.data.SdkWrapperInfo
import com.olo.olopay.data.OloPayEnvironment

/**
 * Interface for mocking/testing purposes
 */
interface IOloPayApiInitializer {
    /** Set up the Olo Pay SDK. See [OloPayApiInitializer] for method documentation */
    fun setup(context: Context, environment: OloPayEnvironment = OloPayEnvironment.Production, callback: InitCompleteCallback)

    /** Set up the Olo Pay SDK. See [OloPayApiInitializer] for method documentation */
    suspend fun setup(context: Context, environment: OloPayEnvironment = OloPayEnvironment.Production)

    /**
     * Convenience object for storing/accessing the Google Pay Config used for Google Pay
     */
    companion object {
        /**
         * The environment the SDK is configured for
         */
        var environment: OloPayEnvironment = OloPayEnvironment.Production
            internal set

        /** @suppress */
        var sdkWrapperInfo: SdkWrapperInfo? = null

        internal val publishableKeyResource: Int
            get() = if (environment == OloPayEnvironment.Production) R.string.prod_publishable_key_url else R.string.dev_publishable_key_url
    }
}