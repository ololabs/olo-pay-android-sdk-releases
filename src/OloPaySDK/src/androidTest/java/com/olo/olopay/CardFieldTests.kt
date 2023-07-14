// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.olo.olopay.data.CardField
import org.junit.Test
import org.junit.Assert.*

import com.stripe.android.view.CardValidCallback.Fields as StripeCardField
import com.stripe.android.view.CardInputListener.FocusField as StripeFocusField

class CardFieldTests {
    @Test
    fun from_StripeCardField_Number_To_OloPayField_CardNumber() {
        assertEquals(CardField.CardNumber, CardField.from(StripeCardField.Number))
    }

    @Test
    fun from_StripeCardField_Expiry_To_OloPayField_Expiration() {
        assertEquals(CardField.Expiration, CardField.from(StripeCardField.Expiry))
    }

    @Test
    fun from_StripeCardField_Cvc_To_OloPayField_Cvc() {
        assertEquals(CardField.Cvc, CardField.from(StripeCardField.Cvc))
    }

    @Test
    fun from_StripeCardField_Postal_To_OloPayField_PostalCode() {
        assertEquals(CardField.PostalCode, CardField.from(StripeCardField.Postal))
    }

    @Test
    fun from_StripeFocusField_CardNumber_To_OloPayField_CardNumber() {
        assertEquals(CardField.CardNumber, CardField.from(StripeFocusField.CardNumber))
    }

    @Test
    fun from_StripeFocusField_ExpiryDate_To_OloPayField_Expiration() {
        assertEquals(CardField.Expiration, CardField.from(StripeFocusField.ExpiryDate))
    }

    @Test
    fun from_StripeFocusField_Cvc_To_OloPayField_Cvc() {
        assertEquals(CardField.Cvc, CardField.from(StripeFocusField.Cvc))
    }

    @Test
    fun from_StripeFocusField_PostalCode_To_OloPayField_PostalCode() {
        assertEquals(CardField.PostalCode, CardField.from(StripeFocusField.PostalCode))
    }

    @Test
    fun cardNumber_getError_withEmptyFlag_returnsEmptyMessage() {
        val expected = "Your card's number is missing"
        assertEquals(expected, CardField.CardNumber.getError(testContext, isEmpty = true))
    }

    @Test
    fun cardNumber_getError_withEmptyFlag_withUnsupportedFlag_returnsEmptyMessage() {
        val expected = "Your card's number is missing"
        assertEquals(expected, CardField.CardNumber.getError(testContext, isEmpty = true, isUnsupported = true))
    }

    @Test
    fun cardNumber_getError_withUnsupportedFlag_returnsUnsupportedMessage() {
        val expected = "Your card type is not supported"
        assertEquals(expected, CardField.CardNumber.getError(testContext, isEmpty = false, isUnsupported = true))
    }

    @Test
    fun cardNumber_getError_withoutFlags_returnsInvalidMessage() {
        val expected = "Your card's number is invalid"
        assertEquals(expected, CardField.CardNumber.getError(testContext, isEmpty = false))
    }

    @Test
    fun expiration_getError_withEmptyFlag_returnsEmptyMessage() {
        val expected = "Your card's expiration date is missing"
        assertEquals(expected, CardField.Expiration.getError(testContext, isEmpty = true))
    }

    @Test
    fun expiration_getError_withEmptyFlag_withUnsupportedFlag_returnsEmptyMessage() {
        val expected = "Your card's expiration date is missing"
        assertEquals(expected, CardField.Expiration.getError(testContext, isEmpty = true, isUnsupported = true))
    }

    @Test
    fun expiration_getError_withUnsupportedFlag_returnsInvalidMessage() {
        val expected = "Your card's expiration date is invalid"
        assertEquals(expected, CardField.Expiration.getError(testContext, isEmpty = false, isUnsupported = true))
    }

    @Test
    fun expiration_getError_withoutFlags_returnsInvalidMessage() {
        val expected = "Your card's expiration date is invalid"
        assertEquals(expected, CardField.Expiration.getError(testContext, isEmpty = false))
    }

    @Test
    fun cvc_getError_withEmptyFlag_returnsEmptyMessage() {
        val expected = "Your card's security code is missing"
        assertEquals(expected, CardField.Cvc.getError(testContext, isEmpty = true))
    }

    @Test
    fun cvc_getError_withEmptyFlag_withUnsupportedFlag_returnsEmptyMessage() {
        val expected = "Your card's security code is missing"
        assertEquals(expected, CardField.Cvc.getError(testContext, isEmpty = true, isUnsupported = true))
    }

    @Test
    fun cvc_getError_withUnsupportedFlag_returnsInvalidMessage() {
        val expected = "Your card's security code is invalid"
        assertEquals(expected, CardField.Cvc.getError(testContext, isEmpty = false, isUnsupported = true))
    }

    @Test
    fun cvc_getError_withoutFlags_returnsInvalidMessage() {
        val expected = "Your card's security code is invalid"
        assertEquals(expected, CardField.Cvc.getError(testContext, isEmpty = false))
    }

    @Test
    fun postalCode_getError_withEmptyFlag_returnsEmptyMessage() {
        val expected = "Your ZIP/postal code is missing"
        assertEquals(expected, CardField.PostalCode.getError(testContext, isEmpty = true))
    }

    @Test
    fun postalCode_getError_withEmptyFlag_withUnsupportedFlag_returnsEmptyMessage() {
        val expected = "Your ZIP/postal code is missing"
        assertEquals(expected, CardField.PostalCode.getError(testContext, isEmpty = true, isUnsupported = true))
    }

    @Test
    fun postalCode_getError_withUnsupportedFlag_returnsInvalidMessage() {
        val expected = "Your ZIP/postal code is invalid"
        assertEquals(expected, CardField.PostalCode.getError(testContext, isEmpty = false, isUnsupported = true))
    }

    @Test
    fun postalCode_getError_withoutFlags_returnsInvalidMessage() {
        val expected = "Your ZIP/postal code is invalid"
        assertEquals(expected, CardField.PostalCode.getError(testContext, isEmpty = false))
    }


    private val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
}