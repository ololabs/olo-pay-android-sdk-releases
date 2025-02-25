// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.controls.callbacks
import com.olo.olopay.data.CardField
import com.olo.olopay.data.ICardFieldState

/**
 * A listener for card input events.
 */
interface CardInputListener {
    /**
     * Called whenever the field of focus changes
     * @param field A [CardField] indicating the newly focused field, or null if the card input control no longer has focus
     * @param fieldStates The current state of all fields
     */
    fun onFocusChange(field: CardField?, fieldStates: Map<CardField, ICardFieldState>) {}

    /**
     * Deprecated. Use [onValidStateChanged] or [onInputChanged] to achieve the same functionality.
     * This will be removed in a future release of the SDK.
     */
    @Deprecated(
        "onFieldComplete() will be removed in a future version of the SDK.",
        ReplaceWith("onInputChanged() or onValidStateChanged()"),
        DeprecationLevel.WARNING
    )
    fun onFieldComplete(field: CardField) {}

    /**
     * Called whenever `isValid` changes on the control
     * @param isValid Whether or not the control is in a valid state
     * @param fieldStates The current state of all fields
     */
    fun onValidStateChanged(isValid: Boolean, fieldStates: Map<CardField, ICardFieldState>){}

    /**
     * Called whenever input changes on the control.
     * @param isValid Whether or not the control is in a valid state
     * @param fieldStates The current state of all fields
     */
    fun onInputChanged(isValid: Boolean, fieldStates: Map<CardField, ICardFieldState>){}
}