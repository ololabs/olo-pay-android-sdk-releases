// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import com.olo.olopaytestharness.models.callbacks.ISettingsChangedListener
import kotlinx.coroutines.flow.StateFlow

interface ICardSettings {
    val displayCardForm: StateFlow<Boolean>
    val useSingleLineCardView: StateFlow<Boolean>
    val postalCodeEnabled: StateFlow<Boolean>
    val logCardInputChanges: StateFlow<Boolean>
    val displayCardErrors: StateFlow<Boolean>
    fun addListener(listener: ISettingsChangedListener<ICardSettings>)
    fun removeListener(listener: ISettingsChangedListener<ICardSettings>)
}