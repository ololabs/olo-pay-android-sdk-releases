// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import android.content.Context
import com.olo.olopaytestharness.R
import kotlinx.coroutines.flow.MutableStateFlow

class UserSettings private constructor(appContext: Context) : Settings<IUserSettings>(), IUserSettings {
    override val useLoggedInUser: MutableStateFlow<Boolean> =
        MutableStateFlow(appContext.resources?.getBoolean(R.bool.user_use_logged_in_user) ?: false)

    override val userEmail: MutableStateFlow<String> =
        MutableStateFlow(appContext.resources?.getString(R.string.user_email) ?: "")

    override val userPassword: MutableStateFlow<String> =
        MutableStateFlow(appContext.resources?.getString(R.string.user_password) ?: "")

    override val savedCardBillingId: MutableStateFlow<String> =
        MutableStateFlow(appContext.resources?.getString(R.string.saved_card_billing_id) ?: "")

    override fun notifySettingsChanged() {
        notifySettingsChanged(this)
    }

    companion object {
        @Volatile private var instance: UserSettings? = null

        fun getInstance(context: Context): UserSettings =
            instance ?: synchronized(this) {
                instance ?: UserSettings(context.applicationContext).also { instance = it }
            }

        fun getReadOnlyInstance(context: Context): IUserSettings = getInstance(context)
    }
}