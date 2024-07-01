// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.internal.data.CardFieldState
import org.junit.Test
import org.junit.Assert.*

class CardFieldStateTests {
    @Test
    fun reset_fieldsResetToDefaultValues() {
        val fieldState = CardFieldState()
        fieldState.setValid(true)
        fieldState.setIsFocused(true)
        fieldState.setEdited(false)
        fieldState.setEdited(true)
        fieldState.setWasFocused(true)

        fieldState.reset()
        assertFalse(fieldState.isValid)
        assertFalse(fieldState.isFocused)
        assertTrue(fieldState.isEmpty)
        assertFalse(fieldState.wasEdited)
        assertFalse(fieldState.wasFocused)
    }
}