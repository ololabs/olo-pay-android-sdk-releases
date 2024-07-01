// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import android.content.Context
import com.olo.olopay.R
import com.olo.olopay.internal.callbacks.ValidStateChangedListener

internal class CvvState(){
    internal val fieldState = CardFieldState()

    internal var validStateChangedListener: ValidStateChangedListener? = null

    internal val isValid: Boolean
        get() { return fieldState.isValid }

    internal val isFocused: Boolean
        get() { return fieldState.isFocused }

    internal fun reset() {
        val previousValidState = fieldState.isValid
        fieldState.reset()
        notifyValidStateChanged(previousValidState)
    }

    internal fun getErrorMessage(context: Context, ignoreUneditedFieldErrors: Boolean): String {
        val hasError = hasErrorMessage(ignoreUneditedFieldErrors)
        return if (hasError && fieldState.isEmpty) {
            context.getString(R.string.olopay_empty_cvv_error)
        } else if (hasError && !fieldState.isEmpty) {
            context.getString(R.string.olopay_incomplete_cvv_error)
        } else {
            ""
        }
    }

    internal fun editingCompleted(){
        fieldState.setEdited(true)
        fieldState.setWasFocused(true)
    }

    internal fun hasErrorMessage(ignoreUneditedFieldErrors: Boolean) : Boolean {
        if(!ignoreUneditedFieldErrors) {
            return !fieldState.isValid
        }

        return !fieldState.isValid && fieldState.wasEdited && fieldState.wasFocused
    }

    internal fun isValidCvvCode(cvvValue: String): Boolean {
        val regEx = """^[0-9]{3,4}$""".toRegex()
        return regEx matches cvvValue
    }

    internal fun onFocusChanged(isFocused: Boolean) {
        val wasFocused = fieldState.isFocused

        // Unless the field was edited, treat it as though the
        // field never entered the focused state (this helps
        // prevent displaying an error prematurely)
        if(wasFocused && !isFocused && fieldState.wasEdited) {
            fieldState.setWasFocused(true)
        }
        fieldState.setIsFocused(isFocused)
    }

    internal fun onInputChanged(newValue: String) {
        fieldState.setEmpty(newValue.isEmpty())

        if(newValue.isNotEmpty()) {
            fieldState.setEdited(true)
        }

        val previousValidState = isValid
        fieldState.setValid(isValidCvvCode(newValue))

        notifyValidStateChanged(previousValidState)
    }

    private fun notifyValidStateChanged(previousValidState: Boolean) {
        if (previousValidState != isValid) {
            validStateChangedListener?.onValidStateChanged(isValid)
        }
    }
}
