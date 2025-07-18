// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.googlepay.GooglePayCheckoutStatus
import org.junit.Test
import org.junit.Assert.*

class GooglePayCheckoutStatusTests {
    @Test
    fun from_String_To_EstimatedDefault() {
        assertEquals(GooglePayCheckoutStatus.EstimatedDefault, GooglePayCheckoutStatus.from("estimateddefault"))
        assertEquals(GooglePayCheckoutStatus.EstimatedDefault, GooglePayCheckoutStatus.from("ESTIMATEDDEFAULT"))
        assertEquals(GooglePayCheckoutStatus.EstimatedDefault, GooglePayCheckoutStatus.from("eStImAtEdDeFaUlT"))
    }

    @Test
    fun from_String_To_FinalDefault() {
        assertEquals(GooglePayCheckoutStatus.FinalDefault, GooglePayCheckoutStatus.from("finaldefault"))
        assertEquals(GooglePayCheckoutStatus.FinalDefault, GooglePayCheckoutStatus.from("FINALDEFAULT"))
        assertEquals(GooglePayCheckoutStatus.FinalDefault, GooglePayCheckoutStatus.from("FiNaLdEfAuLt"))
    }

    @Test
    fun from_String_To_FinalImmediatePurchase() {
        assertEquals(GooglePayCheckoutStatus.FinalImmediatePurchase, GooglePayCheckoutStatus.from("finalimmediatepurchase"))
        assertEquals(GooglePayCheckoutStatus.FinalImmediatePurchase, GooglePayCheckoutStatus.from("FINALIMMEDIATEPURCHASE"))
        assertEquals(GooglePayCheckoutStatus.FinalImmediatePurchase, GooglePayCheckoutStatus.from("fInAlImMeDiAtEpUrChAsE"))
    }

    @Test
    fun from_InvalidString_ReturnsNull() {
        assertNull(GooglePayCheckoutStatus.from(""))
        assertNull(GooglePayCheckoutStatus.from("randomString"))
        assertNull(GooglePayCheckoutStatus.from("Final Immediate Purchase"))
        assertNull(GooglePayCheckoutStatus.from("Final"))
    }
}