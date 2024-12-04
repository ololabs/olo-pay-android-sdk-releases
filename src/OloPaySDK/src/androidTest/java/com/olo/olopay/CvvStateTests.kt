// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.olo.olopay.internal.callbacks.ValidStateChangedListener
import com.olo.olopay.internal.data.CvvState
import org.junit.Test
import org.junit.Assert.*

class CvvStateTests {

    @Test
    fun testConstructor_validateInitialState() {
        val state = CvvState()

        assertFalse(state.isValid)
        assertFalse(state.fieldState.isValid)
        assertTrue(state.fieldState.isEmpty)
        assertFalse(state.fieldState.wasEdited)
        assertFalse(state.fieldState.isFocused)
        assertFalse(state.fieldState.wasFocused)
    }

    @Test
    fun getErrorMessage_ignoreUnedited_withInvalidField_stateNotEmpty_isEdited_wasFocused_returnsIncompleteCvvError() {
        val state = CvvState()
        state.fieldState.setValid(false)
        state.fieldState.setEdited(true)
        state.fieldState.setWasFocused(true)
        state.fieldState.setEmpty(false)

        assertEquals(testContext.getString(R.string.olopay_incomplete_cvv_error), state.getErrorMessage(testContext, true))
    }

    @Test
    fun getErrorMessage_notIgnoreUnedited_withInvalidField_stateNotEmpty_returnsIncompleteCvvError() {
        val state = CvvState()
        state.fieldState.setValid(false)
        state.fieldState.setEmpty(false)

        assertEquals(testContext.getString(R.string.olopay_incomplete_cvv_error), state.getErrorMessage(testContext, false))
    }

    @Test
    fun getErrorMessage_notIgnoreUnedited_withInvalidField_stateEmpty_returnsEmptyCvvError() {
        val state = CvvState()
        state.fieldState.setValid(false)
        state.fieldState.setEmpty(true)

        assertEquals(testContext.getString(R.string.olopay_empty_cvv_error), state.getErrorMessage(testContext, false))
    }

    @Test
    fun getErrorMessage_ignoreUnedited_withValidField_returnsEmptyString() {
        val state = CvvState()
        state.fieldState.setValid(true)

        assertEquals("", state.getErrorMessage(testContext, true))
    }

    @Test
    fun hasErrorMessage_ignoreUnedited_withInvalidField_isEdited_wasFocused_returnsTrue() {
        val state = CvvState()
        state.fieldState.setValid(false)
        state.fieldState.setEdited(true)
        state.fieldState.setWasFocused(true)

        assertTrue(state.hasErrorMessage(true))
    }

    @Test
    fun hasErrorMessage_ignoreUnedited_isEmpty_notIsEdited_notWasFocused_returnsFalse() {
        val state = CvvState()
        state.fieldState.setEdited(false)
        state.fieldState.setWasFocused(false)
        state.fieldState.setEmpty(true)

        assertFalse(state.hasErrorMessage(true))
    }

    @Test
    fun hasErrorMessage_ignoreUnedited_withValidField_isEdited_WasFocused_returnsFalse() {
        val state = CvvState()
        state.fieldState.setValid(true)
        state.fieldState.setEdited(true)
        state.fieldState.setWasFocused(true)

        assertFalse(state.hasErrorMessage(true))
    }

    @Test
    fun hasErrorMessage_notIgnoreUnedited_withValidField_returnsFalse() {
        val state = CvvState()
        state.fieldState.setValid(true)

        assertFalse(state.hasErrorMessage(false))
    }

    @Test
    fun hasErrorMessage_notIgnoreUnedited_withInvalidField_returnsTrue() {
        val state = CvvState()
        state.fieldState.setValid(false)

        assertTrue(state.hasErrorMessage(false))
    }

    @Test
    fun hasErrorMessage_ignoreUnedited_withValidField_wasEdited_notWasFocused_returnsFalse() {
        val state = CvvState()
        state.fieldState.setValid(true)
        state.fieldState.setEdited(true)
        state.fieldState.setWasFocused(false)

        assertFalse(state.hasErrorMessage(true))
    }

    @Test
    fun hasErrorMessage_ignoreUnedited_withValidField_NotIsEdited_WasFocused_returnsFalse() {
        val state = CvvState()
        state.fieldState.setValid(true)
        state.fieldState.setEdited(false)
        state.fieldState.setWasFocused(true)

        assertFalse(state.hasErrorMessage(true))
    }

    @Test
    fun hasErrorMessage_ignoreUnedited_withValidField_notIsEdited_notWasFocused_returnsFalse() {
        val state = CvvState()
        state.fieldState.setValid(true)
        state.fieldState.setEdited(false)
        state.fieldState.setWasFocused(false)

        assertFalse(state.hasErrorMessage(true))
    }

    @Test
    fun hasErrorMessage_ignoreUnedited_withInvalidField_IsEdited_notWasFocused_returnsFalse() {
        val state = CvvState()
        state.fieldState.setValid(false)
        state.fieldState.setEdited(true)
        state.fieldState.setWasFocused(false)

        assertFalse(state.hasErrorMessage(true))
    }

    @Test
    fun hasErrorMessage_ignoreUnedited_withInvalidField_notIsEdited_notWasFocused_returnsFalse() {
        val state = CvvState()
        state.fieldState.setValid(false)
        state.fieldState.setEdited(false)
        state.fieldState.setWasFocused(false)

        assertFalse(state.hasErrorMessage(true))
    }

    @Test
    fun isValidCvvCode_withAlphabetCharacters_returnsFalse() {
        val state = CvvState()
        val cvvValue = "1a3"

        assertFalse(state.isValidCvvCode(cvvValue))
    }

    @Test
    fun isValidCvvCode_withTooFewDigits_returnsFalse() {
        val state = CvvState()
        val cvvValue = "12"

        assertFalse(state.isValidCvvCode(cvvValue))
    }

    @Test
    fun isValidCvvCode_withTooManyDigits_returnsFalse() {
        val state = CvvState()
        val cvvValue = "12345"

        assertFalse(state.isValidCvvCode(cvvValue))
    }

    @Test
    fun isValidCvvCode_withSpecialCharacters_returnsFalse() {
        val state = CvvState()
        val cvvValue = "1#&"

        assertFalse(state.isValidCvvCode(cvvValue))
    }

    @Test
    fun isValidCvvCode_withThreeDigits_returnsTrue() {
        val state = CvvState()
        val cvvValue = "123"

        assertTrue(state.isValidCvvCode(cvvValue))
    }

    @Test
    fun isValidCvvCode_withFourDigits_returnsTrue() {
        val state = CvvState()
        val cvvValue = "1234"

        assertTrue(state.isValidCvvCode(cvvValue))
    }

    @Test
    fun onFocusChanged_fieldGainsFocus_fieldStateIsFocused_fieldStateNotWasFocused() {
        val state = CvvState()
        state.onFocusChanged(true)

        assertTrue(state.fieldState.isFocused)
        assertFalse(state.fieldState.wasFocused)
    }

    @Test
    fun onFocusChanged_fieldLeavesFocus_withFieldEdited_fieldStateNotIsFocused_fieldStateWasFocused() {
        val state = CvvState()
        state.onFocusChanged(true)
        state.fieldState.setEdited(true)
        state.onFocusChanged(false)

        assertFalse(state.fieldState.isFocused)
        assertTrue(state.fieldState.wasFocused)
    }
    @Test
    fun onFocusChanged_fieldLeavesFocus_withFieldNotEdited_fieldStateNotIsFocused_fieldStateNotWasFocused() {
        val state = CvvState()
        state.onFocusChanged(true)
        state.fieldState.setEdited(false)
        state.onFocusChanged(false)

        assertFalse(state.fieldState.isFocused)
        assertFalse(state.fieldState.wasFocused)
    }


    @Test
    fun onInputChanged_textEmpty_FieldEmptyAndNotEditedAndNotIsValid() {
        val state = CvvState()
        state.onInputChanged("")

        val fieldState = state.fieldState
        assertFalse(fieldState.wasEdited)
        assertTrue(fieldState.isEmpty)
        assertFalse(fieldState.isValid)
    }

    @Test
    fun onInputChanged_textNotEmpty_paramsNotValid_fieldEditedAndNotEmptyAndNotIsValid() {
        val state = CvvState()
        state.onInputChanged("Foobar")

        val fieldState = state.fieldState
        assertTrue(fieldState.wasEdited)
        assertFalse(fieldState.isEmpty)
        assertFalse(fieldState.isValid)
    }

    @Test
    fun onInputChanged_textNotEmpty_paramsValid_fieldEditedAndNotEmptyAndIsValid() {
        val state = CvvState()
        state.onInputChanged("123")

        val fieldState = state.fieldState
        assertTrue(fieldState.wasEdited)
        assertFalse(fieldState.isEmpty)
        assertTrue(fieldState.isValid)
    }

    @Test
    fun onInputChanged_isValidToggledTrue_validStateChangedListenerCalled() {
        val state = CvvState()

        var listenerValidParam = false
        state.validStateChangedListener = ValidStateChangedListener {
            listenerValidParam = it
        }

        assertFalse(state.isValid)
        state.onInputChanged("123")
        assertTrue(state.isValid)
        assertTrue(listenerValidParam)
    }

    @Test
    fun onInputChanged_isValidToggledFalse_validStateChangedListenerCalled() {
        val state = CvvState()
        state.onInputChanged("123")

        var listenerValidParam = true
        state.validStateChangedListener = ValidStateChangedListener {
            listenerValidParam = it
        }

        assertTrue(state.isValid)
        state.onInputChanged("12")
        assertFalse(state.isValid)
        assertFalse(listenerValidParam)
    }

    @Test
    fun onInputChanged_isValidNotToggled_validStateChangedListenerNotCalled() {
        val state = CvvState()
        state.onInputChanged("123")

        var listenerCalled = false
        state.validStateChangedListener = ValidStateChangedListener {
            listenerCalled = true
        }

        assertTrue(state.isValid)
        state.onInputChanged("345")
        assertTrue(state.isValid)
        assertFalse(listenerCalled)
    }

    @Test
    fun reset_isValidToggledFalse_validStateChangedListenerCalled() {
        val state = CvvState()
        state.onInputChanged("123")

        var listenerValidParam = true
        state.validStateChangedListener = ValidStateChangedListener {
            listenerValidParam = it
        }

        assertTrue(state.isValid)
        state.reset()
        assertFalse(state.isValid)
        assertFalse(listenerValidParam)
    }

    @Test
    fun reset_isValidNotToggled_validStateChangedListenerNotCalled() {
        val state = CvvState()
        state.onInputChanged("12")

        var listenerCalled = false
        state.validStateChangedListener = ValidStateChangedListener {
            listenerCalled = true
        }

        assertFalse(state.isValid)
        state.reset()
        assertFalse(state.isValid)
        assertFalse(listenerCalled)
    }

    @Test
    fun editingCompleted_updatesState_wasEdited_wasFocused() {
        val state = CvvState()
        state.editingCompleted()

        assertTrue(state.fieldState.wasEdited)
        assertTrue(state.fieldState.wasFocused)
    }

    private val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
}
