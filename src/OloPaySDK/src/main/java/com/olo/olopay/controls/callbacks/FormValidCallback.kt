// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.controls.callbacks

import com.olo.olopay.data.CardField

/**
 * Callback for card input events
 */
fun interface FormValidCallback {
    /**
     * Called whenever input changes on the control.
     * @param isValid Whether or not the control is in a valid state
     * @param invalidFields If [isValid] is false, contains the list of [CardField] fields that are invalid. If [isValid] is true, this is an empty set
     */
    fun onInputChanged(isValid: Boolean, invalidFields: Set<CardField>)
}