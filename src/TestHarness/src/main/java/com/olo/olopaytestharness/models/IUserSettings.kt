// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import com.olo.olopaytestharness.models.callbacks.ISettingsChangedListener
import kotlinx.coroutines.flow.StateFlow

interface IUserSettings {
    val useLoggedInUser: StateFlow<Boolean>
    val userEmail: StateFlow<String>
    val userPassword: StateFlow<String>
    val savedCardBillingId: StateFlow<String>
    fun addListener(listener: ISettingsChangedListener<IUserSettings>)
    fun removeListener(listener: ISettingsChangedListener<IUserSettings>)
}