// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import com.olo.olopaytestharness.models.callbacks.ISettingsChangedListener
import kotlinx.coroutines.flow.StateFlow

interface IOloApiSettings {
    val completePayment: StateFlow<Boolean>
    val baseApiUrl: StateFlow<String>
    val apiKey: StateFlow<String>
    val restaurantId: StateFlow<Int>
    val productId: StateFlow<Int>
    val productQty: StateFlow<Int>
    val googlePayBillingSchemeId: StateFlow<Int>
    fun addListener(listener: ISettingsChangedListener<IOloApiSettings>)
    fun removeListener(listener: ISettingsChangedListener<IOloApiSettings>)
}