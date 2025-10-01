// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.googlepay.GooglePayLineItemStatus
import org.junit.Test
import org.junit.Assert.*

class GooglePayLineItemStatusTests {
    @Test
    fun from_String_To_GooglePayLineItemStatus() {
        assertEquals(GooglePayLineItemStatus.Pending, GooglePayLineItemStatus.from("pending"))
        assertEquals(GooglePayLineItemStatus.Pending, GooglePayLineItemStatus.from("PENDING"))
        assertEquals(GooglePayLineItemStatus.Pending, GooglePayLineItemStatus.from("pEnDiNg"))

        assertEquals(GooglePayLineItemStatus.Final, GooglePayLineItemStatus.from("final"))
        assertEquals(GooglePayLineItemStatus.Final, GooglePayLineItemStatus.from("FINAL"))
        assertEquals(GooglePayLineItemStatus.Final, GooglePayLineItemStatus.from("fInAl"))

        assertNull(GooglePayLineItemStatus.from(""))
        assertNull(GooglePayLineItemStatus.from("randomString"))
    }
}