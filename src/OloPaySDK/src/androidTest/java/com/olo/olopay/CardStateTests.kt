// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.olo.olopay.data.CardField
import com.olo.olopay.internal.data.CardState
import org.junit.Test
import org.junit.Assert.*

class CardStateTests {
    @Test
    fun focusedField_noFocusedFields_returnsNull() {
        assertNull(CardState().focusedField)
    }

    @Test
    fun focusedField_withFocusedField_returnsFocusedField() {
        val state = CardState()
        state.fieldStates[CardField.Expiration]!!.setIsFocused(true)
        assertEquals(CardField.Expiration, state.focusedField)
    }

    @Test
    fun onFieldTextChanged_textEmpty_fieldEditedAndEmpty() {
        val state = CardState()
        state.onFieldTextChanged(CardField.Expiration, "")

        val fieldState = state.fieldStates[CardField.Expiration]
        assertTrue(fieldState!!.wasEdited)
        assertTrue(fieldState!!.isEmpty)
    }

    @Test
    fun onFieldTextChanged_textNotEmpty_fieldEditedAndNotEmpty() {
        val state = CardState()
        state.onFieldTextChanged(CardField.Expiration, "Foobar")

        val fieldState = state.fieldStates[CardField.Expiration]
        assertTrue(fieldState!!.wasEdited)
        assertFalse(fieldState!!.isEmpty)
    }

    @Test
    fun onInputChanged_widgetValid_withInvalidFields_stateUpdated() {
        val invalidFields = setOf(CardField.Cvc, CardField.CardNumber)

        val state = CardState()
        state.postalCodeRequired = false
        state.onInputChanged(true, invalidFields)

        assertTrue(state.stripeWidgetValid)
        assertFalse(state.fieldStates[CardField.CardNumber]!!.isValid)
        assertTrue(state.fieldStates[CardField.Expiration]!!.isValid)
        assertFalse(state.fieldStates[CardField.Cvc]!!.isValid)
        assertTrue(state.fieldStates[CardField.PostalCode]!!.isValid)
    }

    @Test
    fun onInputChanged_widgetInvalid_withInvalidFields_stateUpdated() {
        val invalidFields = setOf(CardField.Expiration, CardField.PostalCode)

        val state = CardState()
        state.onInputChanged(false, invalidFields)

        assertFalse(state.stripeWidgetValid)
        assertTrue(state.fieldStates[CardField.CardNumber]!!.isValid)
        assertFalse(state.fieldStates[CardField.Expiration]!!.isValid)
        assertTrue(state.fieldStates[CardField.Cvc]!!.isValid)
        assertFalse(state.fieldStates[CardField.PostalCode]!!.isValid)
    }

    @Test
    fun onFocusChanged_noPreviousFocusedField_fieldIsFocused() {
        val state = CardState()
        state.onFocusChanged(CardField.Expiration)
        assertTrue(state.fieldStates[CardField.Expiration]!!.isFocused)
        assertEquals(CardField.Expiration, state.focusedField)
    }

    @Test
    fun onFocusChanged_withPreviousFocusedField_previousFieldWasFocused_newFieldIsFocused() {
        val state = CardState()
        state.onFocusChanged(CardField.Expiration)
        state.onFocusChanged(CardField.CardNumber)

        val fieldStates = state.fieldStates
        assertFalse(fieldStates[CardField.Expiration]!!.isFocused)
        assertTrue(fieldStates[CardField.Expiration]!!.wasFocused)
        assertTrue(fieldStates[CardField.CardNumber]!!.isFocused)
        assertEquals(CardField.CardNumber, state.focusedField)
    }

    @Test
    fun getErrorFields_eachField_notValid_wasEdited_wasFocused() {
        val state = CardState()

        val invalidFields = listOf(
            state.fieldStates[CardField.CardNumber]!!,
            state.fieldStates[CardField.Cvc]!!
        )

        //Invalidate the fields
        invalidFields.forEach {
            it.setValid(false)
            it.setEdited(true)
            it.setWasFocused(true)
        }

        val actualInvalidFields = state.getErrorFields(true)
        assertEquals(2, actualInvalidFields.count())
        assertTrue(actualInvalidFields.containsKey(CardField.CardNumber))
        assertTrue(actualInvalidFields.containsKey(CardField.Cvc))

        actualInvalidFields.forEach { (_, fieldState) ->
            assertFalse(fieldState.isValid)
            assertTrue(fieldState.wasEdited)
            assertTrue(fieldState.wasFocused)
        }
    }

    @Test
    fun getErrorMessage_customErrorsNotSupported_withInvalidFields_emptyStringReturned() {
        val state = CardState(false)
        val invalidField = state.fieldStates[CardField.CardNumber]!!
        invalidField.setValid(false)
        invalidField.setEdited(true)
        invalidField.setWasFocused(true)

        assertTrue(state.getErrorMessage(testContext, true).isEmpty())
    }

    @Test
    fun getErrorMessage_customErrorsNotSupported_withAllValidFields_emptyStringReturned() {
        val state = CardState(false)
        state.fieldStates.forEach { (_, fieldState) ->
            fieldState.setValid(true)
        }

        assertTrue(state.getErrorMessage(testContext, true).isEmpty())
    }

    @Test
    fun getErrorMessage_withAllValidFields_emptyStringReturned() {
        val state = CardState()
        state.fieldStates.forEach { (_, fieldState) ->
            fieldState.setValid(true)
        }

        assertTrue(state.getErrorMessage(testContext, true).isEmpty())
    }

    @Test
    fun reset_stateIsReset() {
        val state = CardState()
        state.stripeWidgetValid = true
        state.fieldStates[CardField.CardNumber]!!.setValid(true)
        state.fieldStates[CardField.Cvc]!!.setEmpty(false)
        state.fieldStates[CardField.Expiration]!!.setWasFocused(true)
        state.fieldStates[CardField.PostalCode]!!.setIsFocused(true)

        state.reset()

        assertFalse(state.stripeWidgetValid)
        state.fieldStates.forEach { (_, fieldState) ->
            assertFalse(fieldState.isValid)
            assertFalse(fieldState.isFocused)
            assertTrue(fieldState.isEmpty)
            assertFalse(fieldState.wasEdited)
            assertFalse(fieldState.wasFocused)
        }
    }

    private val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
}