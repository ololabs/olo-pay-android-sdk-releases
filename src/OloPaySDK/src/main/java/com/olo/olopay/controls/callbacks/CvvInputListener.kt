// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.controls.callbacks

import com.olo.olopay.data.ICardFieldState

/**
 * A listener for cvv input events.
 */
interface CvvInputListener {
    /**
     * Called whenever the focus changes on the input
     * @param state The current state of the CVV field
     */
    fun onFocusChange(state: ICardFieldState) {}

    /**
     * Called whenever the field's isValid state changes
     * @param state The current state of the CVV field
     */
    fun onValidStateChanged(state: ICardFieldState) {}

    /**
     * Called whenever input changes on the control
     * @param state The current state of the CVV field
     */
    fun onInputChanged(state: ICardFieldState){}
}
