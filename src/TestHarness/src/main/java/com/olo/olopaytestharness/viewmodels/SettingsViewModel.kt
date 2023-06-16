// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.olo.olopaytestharness.R
import java.lang.Exception

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    //////////////////////////////////////
    // UI SETTINGS
    //////////////////////////////////////
    val displayCardForm =
        MutableLiveData(application.resources.getBoolean(R.bool.display_card_form))

    val useSingleLineCardView =
        MutableLiveData(application.resources.getBoolean(R.bool.use_single_line_card_view))

    val postalCodeEnabled =
        MutableLiveData(application.resources.getBoolean(R.bool.postal_code_enabled))

    val logCardInputChanges =
        MutableLiveData(application.resources.getBoolean(R.bool.log_card_input_changes))

    val displayCardErrors =
        MutableLiveData(application.resources.getBoolean(R.bool.display_card_errors))

    //////////////////////////////////////
    // OLO ORDERING API CLIENT SETTINGS
    //////////////////////////////////////
    val completeOloPayPayment =
        MutableLiveData(application.resources.getBoolean(R.bool.complete_olo_pay_payment))

    val baseApiUrl =
        MutableLiveData(application.getString(R.string.base_api_url))

    val apiKey =
        MutableLiveData(application.getString(R.string.api_key))

    val userEmail =
        MutableLiveData(application.getString(R.string.user_email))

    val restaurantId = MediatorLiveData<Int>()
    val restaurantIdText = MediatorLiveData<String>()

    val productId = MediatorLiveData<Int>()
    val productIdText = MediatorLiveData<String>()

    val productQty = MediatorLiveData<Int>()
    val productQtyText = MediatorLiveData<String>()

    val googlePayBillingSchemeId = MediatorLiveData<Int>()
    val googlePayBillingSchemeIdText = MediatorLiveData<String>()

    init {
        restaurantIdText.addSource(restaurantId) { id ->
            val newId = id.toString()
            val currentId = restaurantIdText.value ?: ""

            if (newId != currentId)
                restaurantIdText.value = id.toString()
        }

        restaurantId.addSource(restaurantIdText) { idText ->
            try {
                val newId = Integer.parseInt(idText)
                val currentId = restaurantId.value ?: 0

                if (newId != currentId)
                    restaurantId.value = newId
            } catch (e: Exception) {
                // Catch exception to prevent crash if user deletes
                // restaurant id in settings... note this means the int
                // version of this property won't change until the user
                // begins typing in the text field again. If they leave
                // the field blank then the actual int value will remain
                // unchanged
            }
        }

        restaurantId.value = application.resources.getInteger(R.integer.restaurant_id)

        productIdText.addSource(productId) { id ->
            val newId = id.toString()
            val currentId = productIdText.value ?: ""

            if (newId != currentId)
                productIdText.value = newId
        }

        productId.addSource(productIdText) { idText ->
            try {
                val newId = Integer.parseInt(idText)
                val currentId = productId.value ?: 0

                if (newId != currentId)
                    productId.value = newId
            } catch (e: Exception) {
                // See catch comment above
            }
        }

        productId.value = application.resources.getInteger(R.integer.product_id)

        productQtyText.addSource(productQty) { id ->
            val newId = id.toString()
            val currentId = productQtyText.value ?: ""

            if (newId != currentId)
                productQtyText.value = newId
        }

        productQty.addSource(productQtyText) { idText ->
            try {
                val newId = Integer.parseInt(idText)
                val currentId = productQty.value ?: 0

                if (newId != currentId)
                    productQty.value = newId
            } catch (e: Exception) {
                // See catch comment above
            }
        }

        productQty.value = application.resources.getInteger(R.integer.product_qty)

        googlePayBillingSchemeIdText.addSource(googlePayBillingSchemeId) { id ->
            val newId = id.toString()
            val currentId = googlePayBillingSchemeIdText.value ?: ""

            if (newId != currentId)
                googlePayBillingSchemeIdText.value = newId
        }

        googlePayBillingSchemeId.addSource(googlePayBillingSchemeIdText) { idText ->
            try {
                val newId = Integer.parseInt(idText)
                val currentId = googlePayBillingSchemeId.value ?: 0

                if (newId != currentId)
                    googlePayBillingSchemeId.value = newId
            } catch (e: Exception) {
                // See catch comment above
            }
        }

        googlePayBillingSchemeId.value = application.resources.getInteger(R.integer.google_pay_billing_scheme_id)
    }
}