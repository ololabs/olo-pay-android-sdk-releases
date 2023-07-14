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
     * @param field A [CardField] indicating the newly focused field
     */
    fun onFocusChange(field: CardField) {}

    /**
     * Called whenever a field is determined to be complete and valid. This may be called multiple times if
     * the user edits the field. If a field is invalid (e.g. an expiration date in the past) then
     * this callback will not get called
     * @param field A [CardField] indicating the field that is complete
     */
    fun onFieldComplete(field: CardField) {}

    /**
     * Called whenever input changes on the control.
     * @param isValid Whether or not the control is in a valid state
     * @param fieldStates The current state of all fields
     */
    fun onInputChanged(isValid: Boolean, fieldStates: Map<CardField, ICardFieldState>){}
}