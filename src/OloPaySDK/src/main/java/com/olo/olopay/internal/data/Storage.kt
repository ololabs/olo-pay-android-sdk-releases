// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.olo.olopay.data.OloPayEnvironment

internal class Storage(context: Context) {
    private val _prefs: SharedPreferences = context.applicationContext.getSharedPreferences(NAME, 0)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var testPublishableKey: String?
        get() { return getString(TEST_CACHE_KEY, null) }
        set(value) { saveString(TEST_CACHE_KEY, value) }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var productionPublishableKey: String?
        get() { return getString(PROD_CACHE_KEY, null) }
        set(value) { saveString(PROD_CACHE_KEY, value) }

    private fun getString(key: String, value: String?): String? {
        return _prefs.getString(key, value)
    }

    private fun saveString(key: String, value: String?) {
        _prefs.edit().putString(key, value).apply()
    }

    fun getPublishableKey(environment: OloPayEnvironment): String? {
        return if(environment == OloPayEnvironment.Test) {
            testPublishableKey
        } else {
            productionPublishableKey
        }
    }

    fun setPublishableKey(environment: OloPayEnvironment, value: String?) {
        if(environment == OloPayEnvironment.Test) {
            testPublishableKey = value
        } else {
            productionPublishableKey = value
        }
    }

    private companion object {
        private val PREFIX = "OloPayAPI"
        private val NAME = "${PREFIX}:${Storage::class.java.canonicalName}"
        private val TEST_CACHE_KEY = "${PREFIX}PublishableKeyTest"
        private val PROD_CACHE_KEY = "${PREFIX}PublishableKeyProd"
    }
}