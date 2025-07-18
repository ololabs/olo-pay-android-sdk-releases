// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.data.CardErrorType
import org.junit.Assert.*
import org.junit.Test

class CardErrorTypeTests {
    @Test
    fun from_InvalidNumberStr_To_InvalidNumber() {
        assertEquals(CardErrorType.InvalidNumber, CardErrorType.from(CardErrorType.InvalidNumberStr))
    }

    @Test
    fun from_InvalidExpMonthStr_To_InvalidExpMonth() {
        assertEquals(CardErrorType.InvalidExpMonth, CardErrorType.from(CardErrorType.InvalidExpMonthStr))
    }

    @Test
    fun from_InvalidExpYearStr_To_InvalidExpYear() {
        assertEquals(CardErrorType.InvalidExpYear, CardErrorType.from(CardErrorType.InvalidExpYearStr))
    }

    @Test
    fun from_InvalidCvcStr_To_InvalidCvv() {
        assertEquals(CardErrorType.InvalidCVV, CardErrorType.from(CardErrorType.InvalidCvcStr))
    }

    @Test
    fun from_IncorrectNumberStr_To_InvalidNumber() {
        assertEquals(CardErrorType.InvalidNumber, CardErrorType.from(CardErrorType.IncorrectNumberStr))
    }

    @Test
    fun from_ExpiredCardStr_To_ExpiredCard() {
        assertEquals(CardErrorType.ExpiredCard, CardErrorType.from(CardErrorType.ExpiredCardStr))
    }

    @Test
    fun from_CardDeclinedStr_To_CardDeclined() {
        assertEquals(CardErrorType.CardDeclined, CardErrorType.from(CardErrorType.CardDeclinedStr))
    }

    @Test
    fun from_IncorrectCvcStr_To_InvalidCvv() {
        assertEquals(CardErrorType.InvalidCVV, CardErrorType.from(CardErrorType.IncorrectCvcStr))
    }

    @Test
    fun from_ProcessingErrorStr_To_ProcessingError() {
        assertEquals(CardErrorType.ProcessingError, CardErrorType.from(CardErrorType.ProcessingErrorStr))
    }

    @Test
    fun from_IncorrectZipStr_To_InvalidZip() {
        assertEquals(CardErrorType.InvalidZip, CardErrorType.from(CardErrorType.IncorrectZipStr))
    }

    @Test
    fun from_InvalidString_to_UnknownCardError() {
        assertEquals(CardErrorType.UnknownCardError, CardErrorType.from("alksjdlkjsdf"))
    }
}