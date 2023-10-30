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
import com.olo.olopaytestharness.models.callbacks.ISettingsChangedListener

class GooglePayViewModel(
    application: Application,
    oloPaySDK: ISDKImplementation,
    logger: ILogger,
    private val oloApiSettings: IOloApiSettings
) : SdkViewModel(application, oloPaySDK, logger), ISettingsChangedListener<IOloApiSettings> {
    private val _googlePayReady = MutableLiveData(false)
    val googlePayReady: LiveData<Boolean> = _googlePayReady

    override fun onResume() {
        oloApiSettings.addListener(this)
    }

    override fun onPause() {
        oloApiSettings.removeListener(this)
    }

    override fun onSettingsChanged(settings: IOloApiSettings) {
        logger.logOloApiSettings(settings, getApplication<Application>().applicationContext)
    }

    fun onGooglePayReady(isReady: Boolean) {
        _googlePayReady.postValue(isReady)
    }

    fun submitGooglePay(context: IGooglePayContext) {
        oloPaySDK.submitGooglePay(context, oloApiSettings)
    }

    fun onGooglePayResult(result: Result) {
        oloPaySDK.onGooglePayResult(result, oloApiSettings)
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
                    OloApiSettings.getReadOnlyInstance(application.applicationContext)
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
                    OloApiSettings.getReadOnlyInstance(application.applicationContext)
                ) as T
            }
        }
    }
}