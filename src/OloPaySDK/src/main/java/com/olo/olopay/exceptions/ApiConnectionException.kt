// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.exceptions

import com.olo.olopay.R
import com.olo.olopay.bootstrap.ApplicationProvider
import com.stripe.android.core.exception.APIConnectionException as StripeApiConnectionException

/**
 * An [Exception] that represents an inability to connect to Olo Pay servers. This could happen if the
 * device has a poor/unstable network connection
 */
class ApiConnectionException : OloPayException {
    internal constructor(exception: StripeApiConnectionException) : super(exception, authMessage){}

    /**
     * Create an instance of this class with the given message
     * @param message The message for the exception
     */
    constructor(message: String?) : super(message) {}

    /**
     * Create an instance of this class with the given message
     * @param message The message for the exception
     */
    constructor(throwable: Throwable) : super(throwable) {}

    /** @suppress */
    companion object {
        private val authMessage: String?
            get() = ApplicationProvider.currentApplication?.getString(R.string.olopay_default_api_error)
    }
}