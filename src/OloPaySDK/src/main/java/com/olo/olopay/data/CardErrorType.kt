// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

/**
 * An enum representing different types of card errors
 */
public enum class CardErrorType {
    /** The card number is invalid (empty, incorrect format, incomplete, etc) */
    InvalidNumber,

    /** The expiration month is invalid */
    InvalidExpMonth,

    /** The expiration year is invalid */
    InvalidExpYear,

    /** The CVV is not valid (empty, incorrect format, incomplete, etc) */
    InvalidCVV,

    /** The postal code is invalid (empty, incorrect format, incomplete, etc). */
    InvalidZip,

    /** The card is expired */
    ExpiredCard,

    /** The card was declined */
    CardDeclined,

    /** An error occurred while processing the card */
    ProcessingError,

    /** An unknown error occurred */
    UnknownCardError;

    /** @suppress */
    companion object {
        internal fun from(stripeString: String?) : CardErrorType {
            return when (stripeString) {
                InvalidNumberStr -> InvalidNumber
                InvalidExpMonthStr -> InvalidExpMonth
                InvalidExpYearStr -> InvalidExpYear
                InvalidCvcStr -> InvalidCVV
                IncorrectNumberStr -> InvalidNumber
                ExpiredCardStr -> ExpiredCard
                CardDeclinedStr -> CardDeclined
                IncorrectCvcStr -> InvalidCVV
                ProcessingErrorStr -> ProcessingError
                IncorrectZipStr -> InvalidZip
                else -> UnknownCardError
            }
        }

        internal const val InvalidNumberStr = "invalid_number"
        internal const val InvalidExpMonthStr = "invalid_expiry_month"
        internal const val InvalidExpYearStr = "invalid_expiry_year"
        internal const val InvalidCvcStr = "invalid_cvc"
        internal const val IncorrectNumberStr = "incorrect_number"
        internal const val ExpiredCardStr = "expired_card"
        internal const val CardDeclinedStr = "card_declined"
        internal const val IncorrectCvcStr = "incorrect_cvc"
        internal const val ProcessingErrorStr = "processing_error"
        internal const val IncorrectZipStr = "incorrect_zip"

    }
}