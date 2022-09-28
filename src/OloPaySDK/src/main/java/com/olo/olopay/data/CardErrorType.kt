// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

/**
 * An enum representing different types of card errors
 */
enum class CardErrorType {
    /** The card number is invalid */
    InvalidNumber,

    /** The expiration month is invalid */
    InvalidExpMonth,

    /** The expiration year is invalid */
    InvalidExpYear,

    /** The CVC security code is invalid */
    InvalidCVC,

    /** The card number is incorrect */
    IncorrectNumber,

    /** The card is expired */
    ExpiredCard,

    /** The card was declined */
    CardDeclined,

    /** The CVC security code is incorrect */
    IncorrectCVC,

    /** An error occurred while processing the card */
    ProcessingError,

    /** The zip/postal code is incorrect */
    IncorrectZip,

    /** An unknown error occurred */
    UnknownCardError;

    /** @suppress */
    companion object {
        internal fun from(string: String?) : CardErrorType {
            return when (string) {
                InvalidNumberStr -> InvalidNumber
                InvalidExpMonthStr -> InvalidExpMonth
                InvalidExpYearStr -> InvalidExpYear
                InvalidCvcStr -> InvalidCVC
                IncorrectNumberStr -> IncorrectNumber
                ExpiredCardStr -> ExpiredCard
                CardDeclinedStr -> CardDeclined
                IncorrectCvcStr -> IncorrectCVC
                ProcessingErrorStr -> ProcessingError
                IncorrectZipStr -> IncorrectZip
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