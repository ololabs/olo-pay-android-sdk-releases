// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import android.content.Context
import com.olo.olopaytestharness.R
import kotlinx.coroutines.flow.MutableStateFlow

class CardSettings private constructor(appContext: Context): Settings<ICardSettings>(), ICardSettings {
    override val displayCardForm: MutableStateFlow<Boolean> =
        MutableStateFlow(appContext.resources?.getBoolean(R.bool.display_card_form) ?: false)

    override val useSingleLineCardView: MutableStateFlow<Boolean> =
        MutableStateFlow(appContext.resources?.getBoolean(R.bool.use_single_line_card_view) ?: true)

    override val postalCodeEnabled: MutableStateFlow<Boolean> =
        MutableStateFlow(appContext.resources?.getBoolean(R.bool.postal_code_enabled) ?: true)

    override val logCardInputChanges: MutableStateFlow<Boolean> =
        MutableStateFlow(appContext.resources?.getBoolean(R.bool.log_card_input_changes) ?: false)

    override val displayCardErrors: MutableStateFlow<Boolean> =
        MutableStateFlow(appContext.resources?.getBoolean(R.bool.display_card_errors) ?: true)


    override fun notifySettingsChanged() {
        notifySettingsChanged(this)
    }

    companion object {
        @Volatile private var instance: CardSettings? = null

        fun getInstance(context: Context): CardSettings =
            instance ?: synchronized(this) {
                instance ?: CardSettings(context.applicationContext).also { instance = it }
            }

        fun getReadOnlyInstance(context: Context): ICardSettings = getInstance(context)
    }
}