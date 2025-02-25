// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.olo.olopay.googlepay.GooglePayLineItem
import com.olo.olopay.googlepay.GooglePayLineItemType
import com.olo.olopay.googlepay.IGooglePayLauncher
import com.olo.olopaytestharness.olopaysdk.ISDKImplementation
import com.olo.olopaytestharness.olopaysdk.JavaOloPayImplementation
import com.olo.olopaytestharness.olopaysdk.KotlinOloPayImplementation
import com.olo.olopaytestharness.models.ILogger
import com.olo.olopaytestharness.models.Logger
import com.olo.olopaytestharness.models.WorkerStatus
import com.olo.olopay.googlepay.GooglePayResult
import com.olo.olopaytestharness.models.GooglePaySettings
import com.olo.olopaytestharness.models.IGooglePaySettings
import com.olo.olopaytestharness.models.IOloApiSettings
import com.olo.olopaytestharness.models.OloApiSettings
import com.olo.olopaytestharness.models.IUserSettings
import com.olo.olopaytestharness.models.UserSettings
import com.olo.olopaytestharness.models.callbacks.ISettingsChangedListener
import com.olo.olopaytestharness.oloapi.createApiClientFromSettings
import com.olo.olopaytestharness.oloapi.createBasketWithProductFromSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.math.roundToInt

class GooglePayViewModel(
    application: Application,
    oloPaySDK: ISDKImplementation,
    logger: ILogger,
    private val oloApiSettings: IOloApiSettings,
    private val userSettings: IUserSettings,
    private val googlePaySettings: IGooglePaySettings
) : SdkViewModel(application, oloPaySDK, logger) {
    private val _googlePayReadyFlow = MutableStateFlow(false)
    private val _showLineItemsFlow = MutableStateFlow(false)
    private val _grandTotalFlow = MutableStateFlow(0)
    private val _taxFlow = MutableStateFlow(0)
    private val _tipFlow = MutableStateFlow(0)
    private val _subtotalFlow = MutableStateFlow(0)

    private val oloApiSettingsChangedListener = ISettingsChangedListener<IOloApiSettings> { settings ->
        logger.logOloApiSettings(settings, getApplication<Application>().applicationContext)
        if(!settings.completePayment.value) {
            updateGrandTotal(DefaultGrandTotal)
        }
        updateShowLineItems(!settings.completePayment.value, googlePaySettings.useLineItems.value)
    }

    private val userSettingsChangedListener = ISettingsChangedListener<IUserSettings> { settings ->
        logger.logUserSettings(settings, getApplication<Application>().applicationContext)
    }

    private val googlePaySettingsChangedListener = ISettingsChangedListener<IGooglePaySettings> { settings ->
        logger.logGooglePaySettings(settings, getApplication<Application>().applicationContext)
        updateShowLineItems(!oloApiSettings.completePayment.value, settings.useLineItems.value)
    }

    val googlePayReady: LiveData<Boolean> = _googlePayReadyFlow.asLiveData(viewModelScope.coroutineContext)
    val showLineItems: LiveData<Boolean> = _showLineItemsFlow.asLiveData(viewModelScope.coroutineContext)
    val grandTotal: LiveData<Int> = _grandTotalFlow.asLiveData(viewModelScope.coroutineContext)
    val tax: LiveData<Int> = _taxFlow.asLiveData(viewModelScope.coroutineContext)
    val tip: LiveData<Int> = _tipFlow.asLiveData(viewModelScope.coroutineContext)
    val subTotal: LiveData<Int> = _subtotalFlow.asLiveData(viewModelScope.coroutineContext)

    private var _internalGrandTotal: Int = 0
    private fun updateGrandTotal(newTotal: Int) {
        _internalGrandTotal = newTotal
        _grandTotalFlow.value = newTotal

        updateTax((newTotal * DefaultTaxRate).roundToInt())
        updateTip((newTotal * DefaultTipRate).roundToInt())
        updateSubtotal(newTotal - _internalTax - _internalTip)
    }

    private var _internalTax: Int = 0
    private fun updateTax(newValue: Int) {
        _internalTax = newValue
        _taxFlow.value = newValue
    }

    private var _internalTip: Int = 0
    private fun updateTip(newValue: Int) {
        _internalTip = newValue
        _tipFlow.value = newValue
    }

    private var _internalSubTotal: Int = 0
    private fun updateSubtotal(newValue: Int) {
        _internalSubTotal = newValue
        _subtotalFlow.value = newValue
    }

    init {
        updateGrandTotal(DefaultGrandTotal)
        updateShowLineItems(!oloApiSettings.completePayment.value, googlePaySettings.useLineItems.value)
    }

    private fun updateShowLineItems(completePayment: Boolean, useLineItems: Boolean) {
        _showLineItemsFlow.update {completePayment && useLineItems}  // Don't show line items when complete payment is enabled
    }

    override fun onResume() {
        oloApiSettings.addListener(oloApiSettingsChangedListener)
        userSettings.addListener(userSettingsChangedListener)
        googlePaySettings.addListener(googlePaySettingsChangedListener)
    }

    override fun onPause() {
        oloApiSettings.removeListener(oloApiSettingsChangedListener)
        userSettings.removeListener(userSettingsChangedListener)
        googlePaySettings.removeListener(googlePaySettingsChangedListener)
    }

    fun onGooglePayReady(isReady: Boolean) {
        _googlePayReadyFlow.update {isReady}
        logger.logText("Google Pay Ready: $isReady")
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun submitGooglePay(launcher: IGooglePayLauncher) {
        val apiClient = createApiClientFromSettings(oloApiSettings)

        if(oloApiSettings.completePayment.value) {

            viewModelScope.launch(Dispatchers.IO) {
                val basket = apiClient.createBasketWithProductFromSettings(oloApiSettings)
                val newTotal = (basket.total * 100).toInt()
                updateGrandTotal(newTotal)
                oloPaySDK.submitGooglePay(launcher, oloApiSettings, userSettings, googlePaySettings, _internalGrandTotal, googlePayLineItems(), basket)
            }
        } else {
            oloPaySDK.submitGooglePay(launcher, oloApiSettings, userSettings, googlePaySettings, _internalGrandTotal, googlePayLineItems(), null)
        }
    }

    private fun googlePayLineItems(): List<GooglePayLineItem>? {
        return if (googlePaySettings.useLineItems.value) {
            listOf(
                GooglePayLineItem(
                    label = "Subtotal",
                    price = _internalSubTotal,
                    type = GooglePayLineItemType.Subtotal
                ),
                GooglePayLineItem(
                    label = "Tax",
                    price = _internalTax,
                    type = GooglePayLineItemType.Tax
                ),
                GooglePayLineItem(
                    label = "Tip",
                    price = _internalTip,
                    type = GooglePayLineItemType.LineItem
                ),
            )
        } else {
            null
        }
    }


    fun onGooglePayResult(result: GooglePayResult) {
        oloPaySDK.onGooglePayResult(result, oloApiSettings, userSettings)
    }

    companion object {
        const val DefaultGrandTotal = 1234
        const val DefaultTaxRate = 0.1
        const val DefaultTipRate = 0.15

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
                    UserSettings.getReadOnlyInstance(application.applicationContext),
                    GooglePaySettings.getReadOnlyInstance(application.applicationContext)
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
                    GooglePaySettings.getReadOnlyInstance(application.applicationContext)
                ) as T
            }
        }
    }
}