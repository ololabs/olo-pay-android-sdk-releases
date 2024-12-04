// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.olo.olopaytestharness.olopaysdk.ISDKImplementation
import com.olo.olopaytestharness.models.ILogger

abstract class SdkViewModel(
    application: Application,
    protected val oloPaySDK: ISDKImplementation,
    protected val logger: ILogger
) : AndroidViewModel(application){
    val logOutput: LiveData<String> = logger.logOutput.asLiveData()
    val isBusy: LiveData<Boolean> = oloPaySDK.workerStatus.isBusy.asLiveData()
    fun clearLog() { logger.clearLog() }
    abstract fun onResume()
    abstract fun onPause()
}