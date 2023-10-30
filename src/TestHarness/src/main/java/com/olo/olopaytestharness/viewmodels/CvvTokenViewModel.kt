// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.CreationExtras
import com.olo.olopay.controls.callbacks.CvvInputListener
import com.olo.olopay.data.ICardFieldState
import com.olo.olopay.data.ICvvTokenParams
import com.olo.olopaytestharness.models.CvvSettings
import com.olo.olopaytestharness.models.ICvvSettings
import com.olo.olopaytestharness.models.ILogger
import com.olo.olopaytestharness.models.OloApiSettings
import com.olo.olopaytestharness.models.IOloApiSettings
import com.olo.olopaytestharness.models.Logger
import com.olo.olopaytestharness.models.WorkerStatus
import com.olo.olopaytestharness.models.callbacks.ISettingsChangedListener
import com.olo.olopaytestharness.olopaysdk.ISDKImplementation
import com.olo.olopaytestharness.olopaysdk.KotlinOloPayImplementation
import com.olo.olopaytestharness.olopaysdk.JavaOloPayImplementation

class CvvTokenViewModel(
    application: Application,
    oloPaySDK: ISDKImplementation,
    logger: ILogger,
    private val oloApiSettings: IOloApiSettings,
    private val cvvSettings: ICvvSettings
) : SdkViewModel(application, oloPaySDK, logger),
    CvvInputListener {

    private val cvvDetailsSubmitHeader: String = "------------ CVV DETAILS SUBMISSION ------------"
    val displayCvvErrors = cvvSettings.displayCvvErrors.asLiveData()

    private val cvvSettingsChangedListener = ISettingsChangedListener<ICvvSettings> { settings ->
        logger.logCvvSettings(settings, getApplication<Application>().applicationContext)
    }

    private val oloApiSettingsChangedListener = ISettingsChangedListener<IOloApiSettings> { settings ->
        logger.logOloApiSettings(settings, getApplication<Application>().applicationContext)
    }

    fun createCvvUpdateToken(params: ICvvTokenParams?) {
        logger.logText(cvvDetailsSubmitHeader)
        oloPaySDK.submitCvv(getApplication(), params)
    }

    override fun onResume() {
         oloApiSettings.addListener(oloApiSettingsChangedListener)
         cvvSettings.addListener(cvvSettingsChangedListener)
    }

    override fun onPause() {
         oloApiSettings.removeListener(oloApiSettingsChangedListener)
         cvvSettings.removeListener(cvvSettingsChangedListener)
    }

    override fun onFocusChange(state: ICardFieldState) {
        logCvvInputChange("CVV Field Focus Changed: IsFocused: ${state.isFocused}")
    }

    override fun onValidStateChanged(state: ICardFieldState) {
        logCvvInputChange("isValid State Changed: ${state.isValid}")
    }

    override fun onInputChanged(state: ICardFieldState) {
        logCvvInputChange("Input Changed: IsValid: ${state.isValid}")
    }

    private fun logCvvInputChange(message: String) {
        if (cvvSettings.logCvvInputChanges.value) {
            logger.logText(message)
        }
    }

    companion object {
        val KotlinFactory: ViewModelProvider.Factory = object: ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val logger = Logger()
                val workerStatus = WorkerStatus()

                return CvvTokenViewModel(
                    application,
                    KotlinOloPayImplementation(logger, workerStatus),
                    logger,
                    OloApiSettings.getReadOnlyInstance(application.applicationContext),
                    CvvSettings.getReadOnlyInstance(application.applicationContext)
                    ) as T
            }
        }

        val JavaFactory: ViewModelProvider.Factory = object: ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val logger = Logger()
                val workerStatus = WorkerStatus()

                return CvvTokenViewModel(
                    application,
                    JavaOloPayImplementation(logger, workerStatus),
                    logger,
                    OloApiSettings.getReadOnlyInstance(application.applicationContext),
                    CvvSettings.getReadOnlyInstance(application.applicationContext)
                    ) as T
            }
        }
    }
}
