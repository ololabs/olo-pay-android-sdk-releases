// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

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
}