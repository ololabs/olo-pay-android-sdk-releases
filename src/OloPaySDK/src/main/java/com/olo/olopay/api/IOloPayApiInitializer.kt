// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.api

import android.content.Context
import com.olo.olopay.R
import com.olo.olopay.data.SdkWrapperInfo
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.data.SetupParameters
import com.olo.olopay.googlepay.Config

/**
 * Interface for mocking/testing purposes
 */
interface IOloPayApiInitializer {
    /** Set up the Olo Pay SDK. See [OloPayApiInitializer] for method documentation */
    fun setup(context: Context, parameters: SetupParameters? = null, callback: InitCompleteCallback)

    /** Set up the Olo Pay SDK. See [OloPayApiInitializer] for method documentation */
    suspend fun setup(context: Context, parameters: SetupParameters? = null)

    /**
     * Convenience object for storing/accessing the Google Pay Config used for Google Pay
     */
    companion object {
        /**
         * Google Pay Configuration used by [com.olo.olopay.googlepay.GooglePayContext]
         */
        var googlePayConfig: Config? = null

        /** @suppress */
        var sdkWrapperInfo: SdkWrapperInfo? = null

        internal var environment: OloPayEnvironment = OloPayEnvironment.Production
        internal var freshSetup = environment == OloPayEnvironment.Test

        internal val publishableKeyResource: Int
            get() = if (environment == OloPayEnvironment.Production) R.string.prod_publishable_key_url else R.string.dev_publishable_key_url
    }
}