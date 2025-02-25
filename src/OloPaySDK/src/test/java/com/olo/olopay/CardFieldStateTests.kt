// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import com.olo.olopay.internal.data.CardFieldState
import org.junit.Test
import org.junit.Assert.*

class CardFieldStateTests {

    @Test
    fun validate_cardFieldState_defaultValues() {
        val fieldState = CardFieldState()
        assertFalse(fieldState.isValid)
        assertFalse(fieldState.isFocused)
        assertTrue(fieldState.isEmpty)
        assertFalse(fieldState.wasEdited)
        assertFalse(fieldState.wasFocused)
    }

    @Test
    fun setValid_cardFieldState_isValidUpdates() {
        val fieldState = CardFieldState()
        fieldState.setValid(true)
        assertTrue(fieldState.isValid)

        fieldState.setValid(false)
        assertFalse(fieldState.isValid)
    }

    @Test
    fun setIsFocused_cardFieldState_isFocusedUpdates() {
        val fieldState = CardFieldState()
        fieldState.setIsFocused(true)
        assertTrue(fieldState.isFocused)

        fieldState.setIsFocused(false)
        assertFalse(fieldState.isFocused)
    }

    @Test
    fun setEmpty_cardFieldState_isEmptyUpdates() {
        val fieldState = CardFieldState()
        fieldState.setEmpty(false)
        assertFalse(fieldState.isEmpty)

        fieldState.setEmpty(true)
        assertTrue(fieldState.isEmpty)
    }

    @Test
    fun setEdited_cardFieldState_wasEditedUpdates() {
        val fieldState = CardFieldState()
        fieldState.setEdited(true)
        assertTrue(fieldState.wasEdited)

        fieldState.setEdited(false)
        assertFalse(fieldState.wasEdited)
    }

    @Test
    fun setWasFocused_cardFieldState_wasFocusedUpdates() {
        val fieldState = CardFieldState()
        fieldState.setWasFocused(true)
        assertTrue(fieldState.wasFocused)

        fieldState.setWasFocused(false)
        assertFalse(fieldState.wasFocused)
    }

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

    @Test
    fun toString_defaultValues_returnsStringRepresentationOfDefaultValues() {
        val fieldState = CardFieldState()
        val expected = "com.olo.olopay.internal.data.CardFieldState(isValid=false, isFocused=false, isEmpty=true, wasEdited=false, wasFocused=false)"
        assertEquals(expected, fieldState.toString())
    }

    @Test
    fun toString_nonDefaultValues_returnsStringRepresentationOfValues() {
        val fieldState = CardFieldState()
        fieldState.setValid(true)
        fieldState.setIsFocused(true)
        fieldState.setEdited(false)
        fieldState.setEdited(true)
        fieldState.setWasFocused(true)

        val expected = "com.olo.olopay.internal.data.CardFieldState(isValid=true, isFocused=true, isEmpty=true, wasEdited=true, wasFocused=true)"
        assertEquals(expected, fieldState.toString())
    }
}