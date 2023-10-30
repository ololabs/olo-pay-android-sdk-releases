// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.olo.olopaytestharness.models.OloApiSettings
import com.olo.olopaytestharness.models.CardSettings
import com.olo.olopaytestharness.models.CvvSettings
import java.lang.Exception

class SettingsViewModel(
    application: Application,
    private val oloApiSettings: OloApiSettings,
    private val cardSettings: CardSettings,
    private val cvvSettings: CvvSettings
) : AndroidViewModel(application) {
    //////////////////////////////////////
    // UI SETTINGS
    //////////////////////////////////////
    val displayCardForm = cardSettings.displayCardForm
    val useSingleLineCardView = cardSettings.useSingleLineCardView
    val postalCodeEnabled = cardSettings.postalCodeEnabled
    val logCardInputChanges = cardSettings.logCardInputChanges
    val displayCardErrors = cardSettings.displayCardErrors
    val logCvvInputChanges = cvvSettings.logCvvInputChanges
    val displayCvvErrors = cvvSettings.displayCvvErrors

    //////////////////////////////////////
    // OLO ORDERING API CLIENT SETTINGS
    //////////////////////////////////////
    val completeOloPayPayment = oloApiSettings.completePayment
    val baseApiUrl = oloApiSettings.baseApiUrl
    val apiKey = oloApiSettings.apiKey
    val userEmail = oloApiSettings.userEmail

    private val restaurantId = MediatorLiveData<Int>()
    val restaurantIdText = MediatorLiveData<String>()

    private val productId = MediatorLiveData<Int>()
    val productIdText = MediatorLiveData<String>()

    private val productQty = MediatorLiveData<Int>()
    val productQtyText = MediatorLiveData<String>()

    private val googlePayBillingSchemeId = MediatorLiveData<Int>()
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

                if (newId != currentId) {
                    oloApiSettings.restaurantId.value = newId
                }
            } catch (e: Exception) {
                // Catch exception to prevent crash if user deletes
                // restaurant id in settings... note this means the int
                // version of this property won't change until the user
                // begins typing in the text field again. If they leave
                // the field blank then the actual int value will remain
                // unchanged
                oloApiSettings.restaurantId.value = 0
            }
        }

        restaurantId.value = oloApiSettings.restaurantId.value

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
                    oloApiSettings.productId.value = newId
            } catch (e: Exception) {
                // See catch comment above
                oloApiSettings.productId.value = 0
            }
        }

        productId.value = oloApiSettings.productId.value

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
                    oloApiSettings.productQty.value = newId
            } catch (e: Exception) {
                // See catch comment above
                oloApiSettings.productQty.value = 0
            }
        }

        productQty.value = oloApiSettings.productQty.value

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
                    oloApiSettings.googlePayBillingSchemeId.value = newId
            } catch (e: Exception) {
                // See catch comment above
                oloApiSettings.googlePayBillingSchemeId.value = 0
            }
        }

        googlePayBillingSchemeId.value = oloApiSettings.googlePayBillingSchemeId.value
    }

    fun notifySettingsChanged() {
        oloApiSettings.notifySettingsChanged()
        cardSettings.notifySettingsChanged()
        cvvSettings.notifySettingsChanged()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object: ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])

                return SettingsViewModel(
                    application,
                    OloApiSettings.getInstance(application.applicationContext),
                    CardSettings.getInstance(application.applicationContext),
                    CvvSettings.getInstance(application.applicationContext)
                ) as T
            }
        }
    }
}