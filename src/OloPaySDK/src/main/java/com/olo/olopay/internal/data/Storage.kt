// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import android.content.Context
import android.content.SharedPreferences

internal class Storage(context: Context) {
    private val _prefs: SharedPreferences = context.applicationContext.getSharedPreferences(NAME, 0)

    var publishableKey: String?
        get() { return getString(PUBLISHABLE_KEY_KEY, null) }
        set(value) { saveString(PUBLISHABLE_KEY_KEY, value) }

    private fun getString(key: String, value: String?): String? {
        return _prefs.getString(key, value)
    }

    private fun saveString(key: String, value: String?) {
        _prefs.edit().putString(key, value).apply()
    }

    private companion object {
        private val PREFIX = "OloPayAPI"
        private val NAME = "${PREFIX}:${Storage::class.java.canonicalName}"
        private val PUBLISHABLE_KEY_KEY = "${PREFIX}PublishableKey"
    }
}