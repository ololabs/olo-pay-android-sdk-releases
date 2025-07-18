// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import android.content.Context
import com.olo.olopaytestharness.R
import kotlinx.coroutines.flow.MutableStateFlow

class GooglePaySettings private constructor(appContext: Context) : Settings<IGooglePaySettings>(), IGooglePaySettings {
    override val usePayNowGooglePayButton: MutableStateFlow<Boolean> =
        MutableStateFlow(appContext.resources?.getBoolean(R.bool.google_pay_immediate_checkout_status) ?: false)
    override val useLineItems: MutableStateFlow<Boolean> =
        MutableStateFlow(appContext.resources?.getBoolean(R.bool.google_pay_show_line_items) ?: false)

    override fun notifySettingsChanged() {
        notifySettingsChanged(this)
    }

    companion object {
        @Volatile private var instance: GooglePaySettings? = null

        fun getInstance(context: Context): GooglePaySettings =
            instance ?: synchronized(this) {
                instance ?: GooglePaySettings(context.applicationContext).also { instance = it }
            }

        fun getReadOnlyInstance(context: Context): IGooglePaySettings = getInstance(context)
    }
}