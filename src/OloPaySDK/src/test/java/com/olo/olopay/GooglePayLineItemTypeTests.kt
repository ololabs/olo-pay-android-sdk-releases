// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.googlepay.GooglePayLineItemType
import org.junit.Test
import org.junit.Assert.*

class GooglePayLineItemTypeTests {
    @Test
    fun from_String_To_GooglePayLineItemType() {
        assertEquals(GooglePayLineItemType.Subtotal, GooglePayLineItemType.from("subtotal"))
        assertEquals(GooglePayLineItemType.Subtotal, GooglePayLineItemType.from("SUBTOTAL"))
        assertEquals(GooglePayLineItemType.Subtotal, GooglePayLineItemType.from("sUbToTal"))

        assertEquals(GooglePayLineItemType.Tax, GooglePayLineItemType.from("tax"))
        assertEquals(GooglePayLineItemType.Tax, GooglePayLineItemType.from("TAX"))
        assertEquals(GooglePayLineItemType.Tax, GooglePayLineItemType.from("tAx"))

        assertEquals(GooglePayLineItemType.LineItem, GooglePayLineItemType.from("line_item"))
        assertEquals(GooglePayLineItemType.LineItem, GooglePayLineItemType.from("LINE_ITEM"))
        assertEquals(GooglePayLineItemType.LineItem, GooglePayLineItemType.from("LiNe_ItEm"))

        assertNull(GooglePayLineItemType.from(""))
        assertNull(GooglePayLineItemType.from("randomString"))
    }
}