// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.data.CurrencyCode
import org.junit.Test
import org.junit.Assert.*

class CurrencyCodeTests {
    @Test
    fun from_String_To_USD() {
        assertEquals(CurrencyCode.USD, CurrencyCode.from("usd"))
        assertEquals(CurrencyCode.USD, CurrencyCode.from("USD"))
        assertEquals(CurrencyCode.USD, CurrencyCode.from("UsD"))
    }
    @Test
    fun from_String_To_CAD() {
        assertEquals(CurrencyCode.CAD, CurrencyCode.from("cad"))
        assertEquals(CurrencyCode.CAD, CurrencyCode.from("CAD"))
        assertEquals(CurrencyCode.CAD, CurrencyCode.from("cAd"))
    }

    @Test
    fun from_InvalidString_ReturnsNull() {
        assertNull(CurrencyCode.from("eur"))
        assertNull(CurrencyCode.from("JPY"))
        assertNull(CurrencyCode.from(""))
        assertNull(CurrencyCode.from("unknown"))
        assertNull(CurrencyCode.from("US D"))
    }
}