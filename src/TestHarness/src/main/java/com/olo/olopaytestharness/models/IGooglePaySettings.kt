// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import com.olo.olopaytestharness.models.callbacks.ISettingsChangedListener
import kotlinx.coroutines.flow.StateFlow

interface IGooglePaySettings {
    val usePayNowGooglePayButton: StateFlow<Boolean>
    val useLineItems: StateFlow<Boolean>
    fun addListener(listener: ISettingsChangedListener<IGooglePaySettings>)
    fun removeListener(listener: ISettingsChangedListener<IGooglePaySettings>)
}