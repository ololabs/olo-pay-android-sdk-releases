// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.CreationExtras
import com.olo.olopay.controls.callbacks.CardInputListener
import com.olo.olopay.controls.callbacks.FormValidCallback
import com.olo.olopay.data.CardField
import com.olo.olopay.data.ICardFieldState
import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopaytestharness.models.*
import com.olo.olopaytestharness.models.callbacks.ISettingsChangedListener
import com.olo.olopaytestharness.olopaysdk.ISDKImplementation
import com.olo.olopaytestharness.olopaysdk.JavaOloPayImplementation
import com.olo.olopaytestharness.olopaysdk.KotlinOloPayImplementation

class CreditCardViewModel(
    application: Application,
    oloPaySDK: ISDKImplementation,
    logger: ILogger,
    private val oloApiSettings: IOloApiSettings,
    private val cardSettings: ICardSettings,
    private val userSettings: IUserSettings
    ) : SdkViewModel(application, oloPaySDK, logger),
    CardInputListener,
    FormValidCallback {
    val displayCardForm = cardSettings.displayCardForm.asLiveData()
    val useSingleLineCardView = cardSettings.useSingleLineCardView.asLiveData()
    val postalCodeEnabled = cardSettings.postalCodeEnabled.asLiveData()
    val displayCardErrors = cardSettings.displayCardErrors.asLiveData()

    private val cardSettingsChangedListener = ISettingsChangedListener<ICardSettings> { settings ->
        logger.logCardSettings(settings, getApplication<Application>().applicationContext)
    }

    private val oloApiSettingsChangedListener = ISettingsChangedListener<IOloApiSettings> { settings ->
        logger.logOloApiSettings(settings, getApplication<Application>().applicationContext)
    }

    private val userSettingsChangedListener = ISettingsChangedListener<IUserSettings> { settings ->
        logger.logUserSettings(settings, getApplication<Application>().applicationContext)
    }

    override fun onResume() {
        oloApiSettings.addListener(oloApiSettingsChangedListener)
        cardSettings.addListener(cardSettingsChangedListener)
        userSettings.addListener(userSettingsChangedListener)
    }

    override fun onPause() {
        oloApiSettings.removeListener(oloApiSettingsChangedListener)
        cardSettings.removeListener(cardSettingsChangedListener)
        userSettings.removeListener(userSettingsChangedListener)
    }

    override fun onFocusChange(field: CardField?, fieldStates: Map<CardField, ICardFieldState>) {
        if (!cardSettings.displayCardForm.value) {
            if (field == null) {
                logCardInputChange("Focus Changed: Card input focus cleared")
            } else {
                logCardInputChange("Card Field Focus Changed: $field")
            }
        }
    }

    override fun onValidStateChanged(isValid: Boolean, fieldStates: Map<CardField, ICardFieldState>) {
        if (!cardSettings.displayCardForm.value) {
            logCardInputChange("isValid State Changed: $isValid")
        }
    }

    override fun onValidStateChanged(isValid: Boolean, invalidFields: Set<CardField>) {
        if (cardSettings.displayCardForm.value) {
            logCardInputChange("isValid State Changed: $isValid")
        }
    }

    override fun onInputChanged(isValid: Boolean, invalidFields: Set<CardField>) {
        if (cardSettings.displayCardForm.value) {
            logCardInputChange("Input Changed: IsValid: $isValid")
        }
    }

    override fun onInputChanged(isValid: Boolean, fieldStates: Map<CardField, ICardFieldState>) {
        if (!cardSettings.displayCardForm.value) {
            logCardInputChange("Input Changed: IsValid: $isValid")
        }
    }

    fun createPaymentMethod(params: IPaymentMethodParams?) {
        val header = if (cardSettings.displayCardForm.value) {
            CardFormSubmitHeader
        } else if (cardSettings.useSingleLineCardView.value) {
            SingleLineCardSubmitHeader
        } else {
            MultiLineCardSubmitHeader
        }

        logger.logText(header)
        oloPaySDK.submitPayment(getApplication(), params, oloApiSettings, userSettings)
    }

    private fun logCardInputChange(message: String) {
        if (cardSettings.logCardInputChanges.value) {
            logger.logText(message)
        }
    }

    companion object {
        private const val SingleLineCardSubmitHeader: String = "---------- SINGLE LINE CARD DETAILS SUBMISSION ----------"
        private const val MultiLineCardSubmitHeader: String = "---------- MULTI LINE CARD DETAILS SUBMISSION ----------"
        private const val CardFormSubmitHeader: String = "------------ CARD FORM SUBMISSION ------------"

        val KotlinFactory: ViewModelProvider.Factory = object: ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val logger = Logger()
                val workerStatus = WorkerStatus()

                return CreditCardViewModel(
                    application,
                    KotlinOloPayImplementation(logger, workerStatus),
                    logger,
                    OloApiSettings.getReadOnlyInstance(application.applicationContext),
                    CardSettings.getReadOnlyInstance(application.applicationContext),
                    UserSettings.getReadOnlyInstance(application.applicationContext)
                ) as T
            }
        }

        val JavaFactory: ViewModelProvider.Factory = object: ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val logger = Logger()
                val workerStatus = WorkerStatus()

                return CreditCardViewModel(
                    application,
                    JavaOloPayImplementation(logger, workerStatus),
                    logger,
                    OloApiSettings.getReadOnlyInstance(application.applicationContext),
                    CardSettings.getReadOnlyInstance(application.applicationContext),
                    UserSettings.getReadOnlyInstance(application.applicationContext)
                ) as T
            }
        }
    }
}