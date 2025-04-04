// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.olo.olopay.data.CardBrand
import com.olo.olopay.data.CardField
import com.olo.olopay.internal.callbacks.PostalCodeValidListener
import com.olo.olopay.internal.callbacks.ValidStateChangedListener
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
    fun isValid_withAllFieldsValid_returnsTrue() {
        val state = CardState()
        state.fieldStates.forEach {
            it.value.setValid(true)
        }

        assertTrue(state.isValid)
    }

    @Test
    fun isValid_initialState_returnsFalse() {
        assertFalse(CardState().isValid)
    }

    @Test
    fun isValid_withInvalidCardNumberField_returnsFalse() {
        val state = CardState()
        state.fieldStates.forEach {
            it.value.setValid(true)
        }

        state.fieldStates[CardField.CardNumber]!!.setValid(false)
        assertFalse(state.isValid)
    }

    @Test
    fun isValid_withInvalidExpirationField_returnsFalse() {
        val state = CardState()
        state.fieldStates.forEach {
            it.value.setValid(true)
        }

        state.fieldStates[CardField.Expiration]!!.setValid(false)
        assertFalse(state.isValid)
    }

    @Test
    fun isValid_withInvalidCvvField_returnsFalse() {
        val state = CardState()
        state.fieldStates.forEach {
            it.value.setValid(true)
        }

        state.fieldStates[CardField.Cvv]!!.setValid(false)
        assertFalse(state.isValid)
    }

    @Test
    fun isValid_withInvalidPostalCodeField_returnsFalse() {
        val state = CardState()
        state.fieldStates.forEach {
            it.value.setValid(true)
        }

        state.fieldStates[CardField.PostalCode]!!.setValid(false)
        assertFalse(state.isValid)
    }

    @Test
    fun onFieldTextChanged_textEmpty_fieldEmptyAndNotEdited() {
        val state = CardState()
        state.onFieldTextChanged(CardField.Expiration, "")

        val fieldState = state.fieldStates[CardField.Expiration]
        assertFalse(fieldState!!.wasEdited)
        assertTrue(fieldState.isEmpty)
    }

    @Test
    fun onFieldTextChanged_textNotEmpty_fieldEditedAndNotEmpty() {
        val state = CardState()
        state.onFieldTextChanged(CardField.Expiration, "Foobar")

        val fieldState = state.fieldStates[CardField.Expiration]
        assertTrue(fieldState!!.wasEdited)
        assertFalse(fieldState.isEmpty)
    }

    @Test
    fun onFieldTextChanged_postalCodeTextValid_fieldValid() {
        val state = CardState()
        state.onFieldTextChanged(CardField.PostalCode, "55056")

        val fieldState = state.fieldStates[CardField.PostalCode]
        assertTrue(fieldState!!.isValid)
    }

    @Test
    fun onFieldTextChanged_postalCodeTextValid_postalCodeValidListenerCalled() {
        val state = CardState()

        var listenerCalled = false
        state.postalCodeValidListener = PostalCodeValidListener {
            listenerCalled = true
        }

        state.onFieldTextChanged(CardField.PostalCode, "55056")
        assertTrue(listenerCalled)
    }

    @Test
    fun onFieldTextChanged_postalCodeTextValid_validStateTogglesTrue_validStateChangedListenerCalled() {
        val state = CardState()
        state.fieldStates.forEach {
            val valid = it.key != CardField.PostalCode
            it.value.setValid(valid)
        }

        var listenerValidParam = false
        state.validStateChangedListener = ValidStateChangedListener {
            listenerValidParam = it
        }

        assertFalse(state.isValid)
        state.onFieldTextChanged(CardField.PostalCode, "55056")
        assertTrue(state.isValid)
        assertTrue(listenerValidParam)
    }

    @Test
    fun onFieldTextChanged_postalCodeTextInvalid_validStateTogglesFalse_validStateChangedListenerCalled() {
        val state = CardState()
        state.fieldStates.forEach {
            it.value.setValid(true)
        }

        var listenerValidParam = true
        state.validStateChangedListener = ValidStateChangedListener {
            listenerValidParam = it
        }

        assertTrue(state.isValid)
        state.onFieldTextChanged(CardField.PostalCode, "550")
        assertFalse(state.isValid)
        assertFalse(listenerValidParam)
    }

    @Test
    fun onFieldTextChanged_postalCodeTextValid_validStateDoesNotToggle_validStateChangedListenerNotCalled() {
        val state = CardState()
        state.fieldStates.forEach {
            it.value.setValid(false)
        }

        var listenerCalled = false
        state.validStateChangedListener = ValidStateChangedListener {
            listenerCalled = true
        }

        assertFalse(state.isValid)
        state.onFieldTextChanged(CardField.PostalCode, "55056")
        assertFalse(state.isValid)
        assertFalse(listenerCalled)
    }

    @Test
    fun onInputChanged_withInvalidFields_stateUpdated() {
        val invalidFields = setOf(CardField.Cvv, CardField.CardNumber)

        val state = CardState()
        state.setPostalCodeEnabled(false)
        state.onInputChanged(CardBrand.Visa, invalidFields)

        assertFalse(state.fieldStates[CardField.CardNumber]!!.isValid)
        assertTrue(state.fieldStates[CardField.Expiration]!!.isValid)
        assertFalse(state.fieldStates[CardField.Cvv]!!.isValid)
        assertTrue(state.fieldStates[CardField.PostalCode]!!.isValid)
    }

    @Test
    fun onInputChanged_withUnsupportedCardType_stateUpdated() {
        val invalidFields: Set<CardField> = setOf()
        val state = CardState()
        state.setPostalCodeEnabled(false)
        state.onInputChanged(CardBrand.Visa, invalidFields)

        assertTrue(state.fieldStates[CardField.CardNumber]!!.isValid)
        assertTrue(state.fieldStates[CardField.Expiration]!!.isValid)
        assertTrue(state.fieldStates[CardField.Cvv]!!.isValid)
        assertTrue(state.fieldStates[CardField.PostalCode]!!.isValid)

        state.onInputChanged(CardBrand.Unknown, invalidFields)

        //Ensure card number state changes
        assertFalse(state.fieldStates[CardField.CardNumber]!!.isValid)

        //Ensure this state didn't change
        assertTrue(state.fieldStates[CardField.Expiration]!!.isValid)
        assertTrue(state.fieldStates[CardField.Cvv]!!.isValid)
        assertTrue(state.fieldStates[CardField.PostalCode]!!.isValid)
    }

    @Test
    fun onInputChanged_withoutInvalidFields_cardNumberFieldToggled_validStateChangedListenerCalled() {
        val state = CardState()

        // This is needed because postal code has validation logic that spans multiple methods in CardState.
        // Without this, the test would fail because the postal code field would get set to invalid, causing
        // the ending state of the test to be incorrect
        mimicTextEntry(state, CardField.PostalCode, "55056", setOf(CardField.CardNumber))

        var listenerValidParam = false
        state.validStateChangedListener = ValidStateChangedListener {
            listenerValidParam = it
        }

        assertFalse(state.isValid)
        state.onInputChanged(CardBrand.Visa, setOf())
        assertTrue(state.isValid)
        assertTrue(listenerValidParam)
    }

    @Test
    fun onInputChanged_withoutInvalidFields_expirationFieldToggled_validStateChangedListenerCalled() {
        val state = CardState()

        // This is needed because postal code has validation logic that spans multiple methods in CardState.
        // Without this, the test would fail because the postal code field would get set to invalid, causing
        // the ending state of the test to be incorrect
        mimicTextEntry(state, CardField.PostalCode, "55056", setOf(CardField.Expiration))

        var listenerValidParam = false
        state.validStateChangedListener = ValidStateChangedListener {
            listenerValidParam = it
        }

        assertFalse(state.isValid)
        state.onInputChanged(CardBrand.Visa, setOf())
        assertTrue(state.isValid)
        assertTrue(listenerValidParam)
    }

    @Test
    fun onInputChanged_withoutInvalidFields_cvvFieldToggled_validStateChangedListenerCalled() {
        val state = CardState()

        // This is needed because postal code has validation logic that spans multiple methods in CardState.
        // Without this, the test would fail because the postal code field would get set to invalid, causing
        // the ending state of the test to be incorrect
        mimicTextEntry(state, CardField.PostalCode, "55056", setOf(CardField.Cvv))

        var listenerValidParam = false
        state.validStateChangedListener = ValidStateChangedListener {
            listenerValidParam = it
        }

        assertFalse(state.isValid)
        state.onInputChanged(CardBrand.Visa, setOf())
        assertTrue(state.isValid)
        assertTrue(listenerValidParam)
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
        state.fieldStates[CardField.Expiration]!!.setEdited(true)
        state.onFocusChanged(CardField.CardNumber)

        val fieldStates = state.fieldStates
        assertFalse(fieldStates[CardField.Expiration]!!.isFocused)
        assertTrue(fieldStates[CardField.Expiration]!!.wasFocused)
        assertTrue(fieldStates[CardField.CardNumber]!!.isFocused)
        assertEquals(CardField.CardNumber, state.focusedField)
    }

    @Test
    fun onFocusChanged_focusCleared_previousFieldWasFocused_noFieldIsFocused() {
        val state = CardState()
        state.onFocusChanged(CardField.Expiration)
        state.onFocusChanged(null)

        assertNull(state.focusedField)
        assertFalse(state.fieldStates[CardField.Expiration]!!.isFocused)
    }

    @Test
    fun getErrorFields_ignoreUneditedFieldsFalse_allFields_valid_returnsEmptyMap() {
        val state = CardState()
        state.fieldStates.forEach {
            it.value.setValid(true)
        }

        assertTrue(state.getErrorFields(false).isEmpty())
    }

    @Test
    fun getErrorFields_ignoreUneditedFieldsFalse_singleField_notValid_returnsSingleField() {
        val state = CardState()
        state.fieldStates.forEach {
            it.value.setValid(true)
        }

        state.fieldStates[CardField.CardNumber]!!.setValid(false)

        val invalidFields = state.getErrorFields(false)
        assertEquals(1, invalidFields.count())
        assertTrue(invalidFields.containsKey(CardField.CardNumber))
        assertFalse(invalidFields[CardField.CardNumber]!!.isValid)
    }

    @Test
    fun getErrorFields_ignoreUneditedFieldsFalse_multipleFields_notValid_returnsMultipleFields() {
        val state = CardState()
        state.fieldStates.forEach {
            it.value.setValid(true)
        }

        state.fieldStates[CardField.Expiration]!!.setValid(false)
        state.fieldStates[CardField.Cvv]!!.setValid(false)

        val invalidFields = state.getErrorFields(false)
        assertEquals(2, invalidFields.count())
        assertTrue(invalidFields.containsKey(CardField.Expiration))
        assertTrue(invalidFields.containsKey(CardField.Cvv))

        invalidFields.forEach { (_, fieldState) ->
            assertFalse(fieldState.isValid)
        }
    }

    @Test
    fun getErrorFields_ignoreUneditedFieldsFalse_allFields_inValid_returnsAllFields() {
        val state = CardState()
        val invalidFields = state.getErrorFields(false)

        assertEquals(state.fieldStates.count(), invalidFields.count())
        assertTrue(invalidFields.containsKey(CardField.CardNumber))
        assertTrue(invalidFields.containsKey(CardField.Expiration))
        assertTrue(invalidFields.containsKey(CardField.Cvv))
        assertTrue(invalidFields.containsKey(CardField.PostalCode))

        invalidFields.forEach { (_, fieldState) ->
            assertFalse(fieldState.isValid)
        }
    }

    @Test
    fun getErrorFields_ignoreUneditedFieldsTrue_allFieldsValid_returnsEmptyMap() {
        val state = CardState()
        state.fieldStates.forEach { (_, fieldState) ->
            fieldState.setValid(true)
            fieldState.setEdited(true)
            fieldState.setWasFocused(true)
        }
        assertTrue(state.getErrorFields(true).isEmpty())
    }

    @Test
    fun getErrorFields_ignoreUneditedFieldsTrue_singleField_notValid_wasEdited_wasFocused_returnsSingleField() {
        val state = CardState()
        val invalidField = state.fieldStates[CardField.CardNumber]!!

        //Invalidate the field
        invalidField.setValid(false)
        invalidField.setEdited(true)
        invalidField.setWasFocused(true)

        val actualInvalidFields = state.getErrorFields(true)
        assertEquals(1, actualInvalidFields.count())
        assertTrue(actualInvalidFields.containsKey(CardField.CardNumber))

        actualInvalidFields.forEach { (_, fieldState) ->
            assertFalse(fieldState.isValid)
            assertTrue(fieldState.wasEdited)
            assertTrue(fieldState.wasFocused)
        }
    }

    @Test
    fun getErrorFields_ignoreUneditedFieldsTrue_multipleFields_notValid_wasEdited_wasFocused_returnsMultipleFields() {
        val state = CardState()
        val invalidFields = listOf(
            state.fieldStates[CardField.CardNumber]!!,
            state.fieldStates[CardField.Cvv]!!
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
        assertTrue(actualInvalidFields.containsKey(CardField.Cvv))

        actualInvalidFields.forEach { (_, fieldState) ->
            assertFalse(fieldState.isValid)
            assertTrue(fieldState.wasEdited)
            assertTrue(fieldState.wasFocused)
        }
    }

    @Test
    fun getErrorFields_ignoreUneditedFieldsTrue_allFields_notValid_wasEdited_wasFocused_returnsAllFields() {
        val state = CardState()
        state.fieldStates.forEach { (_, fieldState) ->
            fieldState.setValid(false)
            fieldState.setEdited(true)
            fieldState.setWasFocused(true)
        }

        val actualInvalidFields = state.getErrorFields(true)
        assertEquals(state.fieldStates.count(), actualInvalidFields.count())
        assertTrue(actualInvalidFields.containsKey(CardField.CardNumber))
        assertTrue(actualInvalidFields.containsKey(CardField.Expiration))
        assertTrue(actualInvalidFields.containsKey(CardField.Cvv))
        assertTrue(actualInvalidFields.containsKey(CardField.PostalCode))

        actualInvalidFields.forEach {
            assertFalse(it.value.isValid)
            assertTrue(it.value.wasEdited)
            assertTrue(it.value.wasFocused)
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
    fun getErrorMessage_customErrorsSupported_withAllValidFields_emptyStringReturned() {
        val state = CardState()
        state.fieldStates.forEach { (_, fieldState) ->
            fieldState.setValid(true)
        }

        assertTrue(state.getErrorMessage(testContext, true).isEmpty())
    }

    @Test
    fun getErrorMessage_customErrorSupported_numberFieldInvalid_returnsNumberErrorMessage() {
        val state = CardState()
        state.fieldStates.forEach { (_, fieldState) ->
            fieldState.setValid(true)
        }

        state.fieldStates[CardField.CardNumber]!!.setValid(false)
        assertEquals(testContext.getString(R.string.olopay_empty_card_number_error), state.getErrorMessage(testContext, false))

        state.fieldStates[CardField.CardNumber]!!.setEmpty(false)
        assertEquals(testContext.getString(R.string.olopay_invalid_card_number_error), state.getErrorMessage(testContext, false))

        state.cardBrand = CardBrand.Unsupported
        assertEquals(testContext.getString(R.string.olopay_unsupported_card_type_error), state.getErrorMessage(testContext, false))
    }

    @Test
    fun getErrorMessage_customErrorSupported_expirationFieldInvalid_returnsExpirationErrorMessage() {
        val state = CardState()
        state.fieldStates.forEach { (_, fieldState) ->
            fieldState.setValid(true)
        }

        state.fieldStates[CardField.Expiration]!!.setValid(false)
        assertEquals(testContext.getString(R.string.olopay_empty_expiration_error), state.getErrorMessage(testContext, false))

        state.fieldStates[CardField.Expiration]!!.setEmpty(false)
        assertEquals(testContext.getString(R.string.olopay_invalid_expiration_error), state.getErrorMessage(testContext, false))
    }

    @Test
    fun getErrorMessage_customErrorSupported_cvvFieldInvalid_returnsCvvErrorMessage() {
        val state = CardState()
        state.fieldStates.forEach { (_, fieldState) ->
            fieldState.setValid(true)
        }

        state.fieldStates[CardField.Cvv]!!.setValid(false)
        assertEquals(testContext.getString(R.string.olopay_empty_cvv_error), state.getErrorMessage(testContext, false))

        state.fieldStates[CardField.Cvv]!!.setEmpty(false)
        assertEquals(testContext.getString(R.string.olopay_invalid_cvv_error), state.getErrorMessage(testContext, false))
    }

    @Test
    fun getErrorMessage_customErrorSupported_postalCodeFieldInvalid_returnsPostalCodeErrorMessage() {
        val state = CardState()
        state.fieldStates.forEach { (_, fieldState) ->
            fieldState.setValid(true)
        }

        state.fieldStates[CardField.PostalCode]!!.setValid(false)
        assertEquals(testContext.getString(R.string.olopay_empty_postal_code_error), state.getErrorMessage(testContext, false))

        state.fieldStates[CardField.PostalCode]!!.setEmpty(false)
        assertEquals(testContext.getString(R.string.olopay_invalid_postal_code_error), state.getErrorMessage(testContext, false))
    }

    @Test
    fun reset_stateIsReset() {
        val state = CardState()
        state.fieldStates[CardField.CardNumber]!!.setValid(true)
        state.fieldStates[CardField.Cvv]!!.setEmpty(false)
        state.fieldStates[CardField.Expiration]!!.setWasFocused(true)
        state.fieldStates[CardField.PostalCode]!!.setIsFocused(true)

        state.reset()

        state.fieldStates.forEach { (_, fieldState) ->
            assertFalse(fieldState.isValid)
            assertFalse(fieldState.isFocused)
            assertTrue(fieldState.isEmpty)
            assertFalse(fieldState.wasEdited)
            assertFalse(fieldState.wasFocused)
        }
    }

    private fun mimicTextEntry(state: CardState, field: CardField, text: String, invalidFields: Set<CardField>) {
        state.onInputChanged(CardBrand.Visa, invalidFields)
        state.onFieldTextChanged(field, text)
    }

    private val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
}