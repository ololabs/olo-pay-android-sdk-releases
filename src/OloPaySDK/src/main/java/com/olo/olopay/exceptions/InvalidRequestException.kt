// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.exceptions
import com.stripe.android.core.exception.InvalidRequestException as StripeInvalidRequestException

/**
 * An exception indicating invalid parameters were used in the request
 */
class InvalidRequestException : OloPayException {
    internal constructor(exception: StripeInvalidRequestException) : super(exception, null)

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
}