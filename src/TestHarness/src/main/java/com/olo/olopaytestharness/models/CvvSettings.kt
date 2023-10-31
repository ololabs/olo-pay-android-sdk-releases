// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import android.content.Context
import com.olo.olopaytestharness.R
import kotlinx.coroutines.flow.MutableStateFlow

class CvvSettings private constructor(appContext: Context): Settings<ICvvSettings>(), ICvvSettings {
    override val logCvvInputChanges: MutableStateFlow<Boolean> =
        MutableStateFlow(appContext.resources?.getBoolean(R.bool.log_card_input_changes) ?: false)

    override val displayCvvErrors: MutableStateFlow<Boolean> =
        MutableStateFlow(appContext.resources?.getBoolean(R.bool.display_cvv_errors) ?: true)

    override fun notifySettingsChanged() {
        notifySettingsChanged(this)
    }

    companion object {
        @Volatile private var instance: CvvSettings? = null

        fun getInstance(context: Context): CvvSettings =
            instance ?: synchronized(this) {
                instance ?: CvvSettings(context.applicationContext).also { instance = it }
            }

        fun getReadOnlyInstance(context: Context): ICvvSettings = getInstance(context)
    }
}
