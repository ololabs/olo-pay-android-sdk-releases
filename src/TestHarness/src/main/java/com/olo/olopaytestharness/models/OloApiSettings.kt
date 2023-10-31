// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import android.content.Context
import com.olo.olopaytestharness.R
import kotlinx.coroutines.flow.MutableStateFlow

class OloApiSettings private constructor(appContext: Context) : Settings<IOloApiSettings>(), IOloApiSettings {
    override val completePayment: MutableStateFlow<Boolean> =
        MutableStateFlow(appContext.resources?.getBoolean(R.bool.complete_olo_pay_payment) ?: false)

    override val baseApiUrl: MutableStateFlow<String> =
        MutableStateFlow(appContext.resources?.getString(R.string.base_api_url) ?: "")

    override val apiKey: MutableStateFlow<String> =
        MutableStateFlow(appContext.resources?.getString(R.string.api_key) ?: "")

    override val userEmail: MutableStateFlow<String> =
        MutableStateFlow(appContext.resources?.getString(R.string.user_email) ?: "")

    override val restaurantId: MutableStateFlow<Int> =
        MutableStateFlow(appContext.resources?.getInteger(R.integer.restaurant_id) ?: 0)

    override val productId: MutableStateFlow<Int> =
        MutableStateFlow(appContext.resources?.getInteger(R.integer.product_id) ?: 0)

    override val productQty: MutableStateFlow<Int> =
        MutableStateFlow(appContext.resources?.getInteger(R.integer.product_qty) ?: 0)

    override val googlePayBillingSchemeId: MutableStateFlow<Int> =
        MutableStateFlow(appContext.resources?.getInteger(R.integer.google_pay_billing_scheme_id) ?: 0)

    override fun notifySettingsChanged() {
        notifySettingsChanged(this)
    }

    companion object {
        @Volatile private var instance: OloApiSettings? = null

        fun getInstance(context: Context): OloApiSettings =
            instance ?: synchronized(this) {
                instance ?: OloApiSettings(context.applicationContext).also { instance = it }
            }

        fun getReadOnlyInstance(context: Context): IOloApiSettings = getInstance(context)
    }
}