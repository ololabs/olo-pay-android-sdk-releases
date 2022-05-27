// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.olo.olopay.data.CardField

internal class CardState(val supportsCustomErrors: Boolean = true) {
    var fieldStates = mapOf(
        CardField.CardNumber to CardFieldState(),
        CardField.Expiration to CardFieldState(),
        CardField.Cvc to CardFieldState(),
        CardField.PostalCode to CardFieldState()
    )

    var stripeWidgetValid = false
    var postalCodeRequired = true
    var postalCodeEnabled = true

    val focusedField: CardField?
        get() {
            val fields = fieldStates.filterKeys { fieldStates[it]!!.isFocused }
            return fields.firstNotNullOfOrNull {
                return if (it.value.isFocused) it.key else null
            }
        }

    val isValid
        get() = stripeWidgetValid

    fun hasErrorMessage(ignoreUneditedFieldErrors: Boolean) : Boolean {
        return supportsCustomErrors && getErrorFields(ignoreUneditedFieldErrors).isNotEmpty()
    }

    fun onFieldTextChanged(field: CardField, newText: String) {
        val state = fieldStates[field]!!
        state.setEdited(true)
        state.setEmpty(newText.isEmpty())
    }

    fun onInputChanged(widgetValid: Boolean, invalidFields: Set<CardField>) {
        stripeWidgetValid = widgetValid

        fieldStates.forEach { (key, value) ->
            fieldStates[key]!!.setValid(!invalidFields.contains(key))
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
                CardField.CardNumber.getError(context, invalidStates[CardField.CardNumber]!!.isEmpty)
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
}