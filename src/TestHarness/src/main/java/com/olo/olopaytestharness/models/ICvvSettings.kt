// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import com.olo.olopaytestharness.models.callbacks.ISettingsChangedListener
import kotlinx.coroutines.flow.StateFlow

interface ICvvSettings {
    val logCvvInputChanges: StateFlow<Boolean>
    val displayCvvErrors: StateFlow<Boolean>
    fun addListener(listener: ISettingsChangedListener<ICvvSettings>)
    fun removeListener(listener: ISettingsChangedListener<ICvvSettings>)
}
