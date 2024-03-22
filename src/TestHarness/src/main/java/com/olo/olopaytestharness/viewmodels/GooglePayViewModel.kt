// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.olo.olopay.googlepay.IGooglePayContext
import com.olo.olopaytestharness.olopaysdk.ISDKImplementation
import com.olo.olopaytestharness.olopaysdk.JavaOloPayImplementation
import com.olo.olopaytestharness.olopaysdk.KotlinOloPayImplementation
import com.olo.olopaytestharness.models.ILogger
import com.olo.olopaytestharness.models.Logger
import com.olo.olopaytestharness.models.WorkerStatus
import com.olo.olopay.googlepay.Result
import com.olo.olopaytestharness.models.IOloApiSettings
import com.olo.olopaytestharness.models.OloApiSettings
import com.olo.olopaytestharness.models.IUserSettings
import com.olo.olopaytestharness.models.UserSettings
import com.olo.olopaytestharness.models.callbacks.ISettingsChangedListener

class GooglePayViewModel(
    application: Application,
    oloPaySDK: ISDKImplementation,
    logger: ILogger,
    private val oloApiSettings: IOloApiSettings,
    private val userSettings: IUserSettings
) : SdkViewModel(application, oloPaySDK, logger) {
    private val _googlePayReady = MutableLiveData(false)
    val googlePayReady: LiveData<Boolean> = _googlePayReady

    private val oloApiSettingsChangedListener = ISettingsChangedListener<IOloApiSettings> { settings ->
        logger.logOloApiSettings(settings, getApplication<Application>().applicationContext)
    }

    private val userSettingsChangedListener = ISettingsChangedListener<IUserSettings> { settings ->
        logger.logUserSettings(settings, getApplication<Application>().applicationContext)
    }

    override fun onResume() {
        oloApiSettings.addListener(oloApiSettingsChangedListener)
        userSettings.addListener(userSettingsChangedListener)
    }

    override fun onPause() {
        oloApiSettings.removeListener(oloApiSettingsChangedListener)
        userSettings.removeListener(userSettingsChangedListener)
    }

    fun onGooglePayReady(isReady: Boolean) {
        _googlePayReady.postValue(isReady)
    }

    fun submitGooglePay(context: IGooglePayContext) {
        oloPaySDK.submitGooglePay(context, oloApiSettings, userSettings)
    }

    fun onGooglePayResult(result: Result) {
        oloPaySDK.onGooglePayResult(result, oloApiSettings, userSettings)
    }

    companion object {
        val KotlinFactory: ViewModelProvider.Factory = object: ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val logger = Logger()
                val workerStatus = WorkerStatus()

                return GooglePayViewModel(
                    application,
                    KotlinOloPayImplementation(logger, workerStatus),
                    logger,
                    OloApiSettings.getReadOnlyInstance(application.applicationContext),
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

                return GooglePayViewModel(
                    application,
                    JavaOloPayImplementation(logger, workerStatus),
                    logger,
                    OloApiSettings.getReadOnlyInstance(application.applicationContext),
                    UserSettings.getReadOnlyInstance(application.applicationContext),
                ) as T
            }
        }
    }
}