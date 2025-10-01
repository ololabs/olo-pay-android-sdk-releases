// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.exceptions

import com.stripe.android.core.exception.StripeException

/**
 * Base [Exception] class for all exceptions
 */
open class OloPayException : Exception {
    private val stripeException: StripeException?

    internal constructor(stripeException: StripeException?, message: String? = null) : super(message ?: stripeException?.message, stripeException?.cause) {
        this.stripeException = stripeException
    }

    /**
     * Create an instance of this class with the given message
     * @param message The message for the exception
     */
    constructor(message: String?) : super(message) {
        stripeException = null
    }

    /**
     * Create an instance of this class with the given throwable
     * @param throwable The throwable for the exception
     */
    constructor(throwable: Throwable) : super(throwable) {
        stripeException = null
    }

    /** @suppress */
    override fun toString(): String {
        return "${this.javaClass.name}(message=${message})"
    }
}