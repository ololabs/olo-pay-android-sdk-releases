// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.exceptions

import com.olo.olopay.R
import com.olo.olopay.bootstrap.ApplicationProvider
import com.stripe.android.core.exception.RateLimitException as StripeRateLimitException

/**
 * An exception indicating the rate limit has been exceeded and you need to wait to try again
 */
class RateLimitException : OloPayException {
    internal constructor(exception: StripeRateLimitException) : super(exception, authMessage) {}

    /**
     * Create an instance of this class with the given message
     * @param message The message for the exception
     */
    constructor(message: String?) : super(message) {}

    /**
     * Create an instance of this class with the given throwable
     * @param throwable The throwable for the exception
     */
    constructor(throwable: Throwable) : super(throwable) {}

    /** @suppress */
    companion object {
        private val authMessage: String?
            get() = ApplicationProvider.currentApplication?.getString(R.string.olopay_default_api_error)
    }
}