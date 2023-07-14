// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.olo.olopay.data.CardBrand
import com.olo.olopay.data.CardField
import com.olo.olopay.internal.callbacks.PostalCodeValidListener

internal class CardState(val supportsCustomErrors: Boolean = true) {
    var fieldStates = mapOf(
        CardField.CardNumber to CardFieldState(),
        CardField.Expiration to CardFieldState(),
        CardField.Cvc to CardFieldState(),
        CardField.PostalCode to CardFieldState()
    )

    var postalCodeValidListener: PostalCodeValidListener? = null

    internal var stripeWidgetValid = false
    internal var postalCodeEnabled = true
    private var cardBrand = CardBrand.Unknown
    private var postalCodeTextValid = false
    private val isValidCardBrand
        get() = cardBrand != CardBrand.Unknown && cardBrand != CardBrand.Unsupported

    val postalCodeValid
        get() = if (!postalCodeEnabled) true else postalCodeTextValid

    val focusedField: CardField?
        get() {
            val fields = fieldStates.filterKeys { fieldStates[it]!!.isFocused }
            return fields.firstNotNullOfOrNull {
                return if (it.value.isFocused) it.key else null
            }
        }

    val isValid: Boolean
        get() {
            if (!isValidCardBrand) {
                return false
            }

            return if (!postalCodeEnabled) stripeWidgetValid else stripeWidgetValid && postalCodeValid
        }

    fun hasErrorMessage(ignoreUneditedFieldErrors: Boolean) : Boolean {
        return supportsCustomErrors && getErrorFields(ignoreUneditedFieldErrors).isNotEmpty()
    }

    fun onFieldTextChanged(field: CardField, newText: String) {
        val state = fieldStates[field]!!
        state.setEmpty(newText.isEmpty())

        if (newText.isNotEmpty())
            state.setEdited(true)

        if (field == CardField.PostalCode) {
            postalCodeTextValid = isValidUsPostalCode(newText) || isValidCaPostalCode(newText)
            state.setValid(postalCodeValid)

            if (postalCodeValid) {
                postalCodeValidListener?.onPostalCodeValid()
            }
        }
    }

    fun onInputChanged(widgetValid: Boolean, brand: CardBrand, invalidFields: Set<CardField>) {
        stripeWidgetValid = widgetValid
        cardBrand = brand

        fieldStates.forEach { (key, value) ->
            if (key == CardField.PostalCode) {
                fieldStates[key]!!.setValid(postalCodeValid)
            } else if (key == CardField.CardNumber && !invalidFields.contains(key)) {
                // If Stripe indicates the card number is valid, we need to override that
                // and display an error if the card brand is a type not supported by Olo Pay
                fieldStates[key]!!.setValid(isValidCardBrand)
            } else {
                fieldStates[key]!!.setValid(!invalidFields.contains(key))
            }
        }
    }

    fun onFocusChanged(field: CardField) {
        val previousFocusedField = focusedField
        if (previousFocusedField != null) {
            val previousFocusedState = fieldStates[previousFocusedField]!!
            previousFocusedState.setIsFocused(false)
            previousFocusedState.setWasFocused(true)
        }

        val state = fieldStates[field]!!
        state.setIsFocused(true)
    }

    fun reset() {
        fieldStates.forEach { it.value.reset() }
        stripeWidgetValid = false
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
            invalidStates.containsKey(CardField.Cvc) -> {
                CardField.Cvc.getError(context, invalidStates[CardField.Cvc]!!.isEmpty)
            }
            invalidStates.containsKey(CardField.PostalCode) -> {
                CardField.PostalCode.getError(context, invalidStates[CardField.PostalCode]!!.isEmpty)
            }
            else -> { "" }
        }
    }

    @VisibleForTesting
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
}