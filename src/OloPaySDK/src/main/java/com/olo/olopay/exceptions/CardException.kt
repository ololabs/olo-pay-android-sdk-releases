// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.exceptions

import com.olo.olopay.data.CardErrorType
import com.stripe.android.exception.CardException as StripeCardException

/**
 * An [Exception] indicating that there is a problem with the card used for the request. Card errors
 * are the most common type of error that need to be handled. They occur when the user enters a card
 * that can't be charged for some reason.
 */
class CardException : OloPayException {
    /** The type of card error */
    val type: CardErrorType

    /**
     * For card errors resulting from a card issuer decline, a short string indicating the card
     * issuer’s reason for the decline (if they provide one)
     * */
    val declineCode: String?

    /** The ID of the failed charge */
    val charge: String?

    internal constructor(exception: StripeCardException?) : super(exception) {
        type = CardErrorType.from(exception?.code)
        declineCode = exception?.declineCode
        charge = exception?.declineCode
    }

    internal constructor(type: CardErrorType, message: String?) : super(message) {
        this.type = type
        this.declineCode = null
        this.charge = null
    }

    /**
     * Create an instance of this class with the given message
     * @param message The message for the exception
     * @param type The type of card error this represents
     * @param declineCode The decline code for this error, if one exists
     * @param charge The charge code for this error, if one exists
     */
    constructor(message: String?, type: CardErrorType, declineCode: String?, charge: String?) : super(message) {
        this.type = type
        this.declineCode = declineCode
        this.charge = charge
    }

    /**
     * Create an instance of this class with the given throwable
     * @param throwable The throwable for the exception
     * @param type The type of card error this represents
     * @param declineCode The decline code for this error, if one exists
     * @param charge The charge code for this error, if one exists
     */
    constructor(throwable: Throwable, type: CardErrorType, declineCode: String?, charge: String?) : super(throwable) {
        this.type = type
        this.declineCode = declineCode
        this.charge = charge
    }

    /** @suppress */
    override fun toString(): String {
        val properties = listOf<String>(
            "message=${message}",
            "${CardException::type.name}=${type}",
            "${CardException::declineCode.name}=${declineCode}",
            "${CardException::charge.name}=${charge}"
        )

        return "${this.javaClass.name}(${properties.joinToString(", ")})"
    }
}