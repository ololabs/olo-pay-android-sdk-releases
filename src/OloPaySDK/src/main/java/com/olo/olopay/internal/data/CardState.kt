// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.olo.olopay.data.CardBrand
import com.olo.olopay.data.CardField
import com.olo.olopay.internal.callbacks.PostalCodeValidListener
import com.olo.olopay.internal.callbacks.ValidStateChangedListener

internal class CardState(val supportsCustomErrors: Boolean = true) {
    var fieldStates = mapOf(
        CardField.CardNumber to CardFieldState(),
        CardField.Expiration to CardFieldState(),
        CardField.Cvv to CardFieldState(),
        CardField.PostalCode to CardFieldState()
    )

    var validStateChangedListener: ValidStateChangedListener? = null
    var postalCodeValidListener: PostalCodeValidListener? = null

    private var _postalCodeEnabled = true
    val postalCodeEnabled
        get() = _postalCodeEnabled

    @VisibleForTesting
    internal var cardBrand = CardBrand.Unknown
    private var postalCodeTextValid = false
    private val isValidCardBrand
        get() = cardBrand != CardBrand.Unknown && cardBrand != CardBrand.Unsupported

    private val postalCodeFieldValid
        get() = if (!_postalCodeEnabled) true else postalCodeTextValid

    @VisibleForTesting
    internal val focusedField: CardField?
        get() {
            val fields = fieldStates.filterKeys { fieldStates[it]!!.isFocused }
            return fields.firstNotNullOfOrNull {
                return if (it.value.isFocused) it.key else null
            }
        }

    val isValid: Boolean
        get() {
            return fieldStates.filterKeys { !fieldStates[it]!!.isValid }.isEmpty()
        }

    fun hasErrorMessage(ignoreUneditedFieldErrors: Boolean) : Boolean {
        return supportsCustomErrors && getErrorFields(ignoreUneditedFieldErrors).isNotEmpty()
    }

    fun setPostalCodeEnabled(enabled: Boolean) {
        _postalCodeEnabled = enabled
        fieldStates[CardField.PostalCode]!!.setValid(postalCodeFieldValid)
    }

    fun onFieldTextChanged(field: CardField, newText: String) {
        val previousValidState = isValid

        val state = fieldStates[field]!!
        state.setEmpty(newText.isEmpty())

        if (newText.isNotEmpty())
            state.setEdited(true)

        if (field == CardField.PostalCode) {
            postalCodeTextValid = isValidUsPostalCode(newText) || isValidCaPostalCode(newText)
            state.setValid(postalCodeFieldValid)

            if (postalCodeFieldValid) {
                postalCodeValidListener?.onPostalCodeValid()
            }
        }

        notifyValidStateChanged(previousValidState)
    }

    fun onInputChanged(brand: CardBrand, invalidFields: Set<CardField>) {
        val previousValidState = isValid

        cardBrand = brand

        fieldStates.forEach { (key, value) ->
            if (key == CardField.PostalCode) {
                value.setValid(postalCodeFieldValid)
            } else if (key == CardField.CardNumber) {
                // If Stripe indicates the card number is valid, we need to override that
                // and display an error if the card brand is a type not supported by Olo Pay
                value.setValid(!invalidFields.contains(key) && isValidCardBrand)
            } else {
                value.setValid(!invalidFields.contains(key))
            }
        }

        notifyValidStateChanged(previousValidState)
    }

    fun onFocusChanged(field: CardField?) {
        val previousFocusedField = focusedField

        if (previousFocusedField != null) {
            val previousFocusedState = fieldStates[previousFocusedField]!!

            // Prevent fields from entering error states if focus changes
            // prior to any text being entered in the field
            if (previousFocusedState.wasEdited)
                previousFocusedState.setWasFocused(true)

            previousFocusedState.setIsFocused(false)
        }

        if (field != null) {
            val state = fieldStates[field]!!
            state.setIsFocused(true)
        }
    }

    fun reset() {
        val previousValidState = isValid

        fieldStates.forEach { it.value.reset() }
        cardBrand = CardBrand.Unknown
        postalCodeTextValid = false

        notifyValidStateChanged(previousValidState)
    }

    fun getErrorMessage(context: Context, ignoreUneditedFieldErrors: Boolean): String {
        if (!supportsCustomErrors)
            return ""

        val invalidStates = getErrorFields(ignoreUneditedFieldErrors)

        return when {
            invalidStates.containsKey(CardField.CardNumber) -> {
                CardField.CardNumber.getError(context, invalidStates[CardField.CardNumber]!!.isEmpty, cardBrand == CardBrand.Unsupported)
            }
            invalidStates.containsKey(CardField.Expiration) -> {
                CardField.Expiration.getError(context, invalidStates[CardField.Expiration]!!.isEmpty)
            }
            invalidStates.containsKey(CardField.Cvv) -> {
                CardField.Cvv.getError(context, invalidStates[CardField.Cvv]!!.isEmpty)
            }
            invalidStates.containsKey(CardField.PostalCode) -> {
                CardField.PostalCode.getError(context, invalidStates[CardField.PostalCode]!!.isEmpty)
            }
            else -> { "" }
        }
    }

    internal fun getErrorFields(ignoreUneditedFieldErrors: Boolean): Map<CardField, CardFieldState> {
        // Return all error fields regardless of state
        if (!ignoreUneditedFieldErrors)
            return fieldStates.filterKeys {
                !fieldStates[it]!!.isValid
            }

        // Only return invalid fields that have also been edited and focused
        return fieldStates.filterKeys {
            val state = fieldStates[it]!!
            !state.isValid && state.wasEdited && state.wasFocused
        }
    }

    private fun isValidUsPostalCode(postalCode: String): Boolean {
        val regEx = """^\s*[0-9]{5}(-[0-9]{4})?\s*$""".toRegex()
        return regEx matches postalCode
    }

    private fun isValidCaPostalCode(postalCode: String): Boolean {
        val regEx = """^[ABCEGHJKLMNPRSTVXY][0-9][ABCEGHJKLMNPRSTVWXYZ]\s?[0-9][ABCEGHJKLMNPRSTVWXYZ][0-9]$""".toRegex(RegexOption.IGNORE_CASE)
        return regEx matches postalCode
    }

    private fun notifyValidStateChanged(previousValidState: Boolean) {
        if (previousValidState != isValid) {
            validStateChangedListener?.onValidStateChanged(isValid)
        }
    }
}