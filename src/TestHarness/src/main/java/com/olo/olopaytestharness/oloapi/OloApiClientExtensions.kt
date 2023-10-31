// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi

import com.olo.olopay.api.ApiResultCallback
import com.olo.olopay.data.IPaymentMethod
import com.olo.olopaytestharness.models.IOloApiSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import java.lang.IllegalStateException

@ExperimentalSerializationApi
fun createApiClientFromSettings(settings: IOloApiSettings): OloApiClient {
    val apiUrl = settings.baseApiUrl.value
    val apiKey = settings.apiKey.value

    return OloApiClient(apiUrl, apiKey)
}

@ExperimentalSerializationApi
suspend fun OloApiClient.submitBasketFromSettings(settings: IOloApiSettings, paymentMethod: IPaymentMethod, basketId: String) : Order {
    val email = settings.userEmail.value
    val billingMethod = if (paymentMethod.isGooglePay) "digitalwallet" else "creditcardtoken"
    val billingSchemeId = if (paymentMethod.isGooglePay) settings.googlePayBillingSchemeId.value else null

    return submitBasket(paymentMethod, basketId, email, billingMethod, billingSchemeId)
}

@ExperimentalSerializationApi
fun OloApiClient.submitBasketFromSettings(settings: IOloApiSettings, paymentMethod: IPaymentMethod, basketId: String, callback: ApiResultCallback<Order>) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val order = submitBasketFromSettings(settings, paymentMethod, basketId)
            callback.onSuccess(order)
        } catch (e: Exception) {
            callback.onError(e)
        }
    }
}

@ExperimentalSerializationApi
fun OloApiClient.createBasketWithProductFromSettings(settings: IOloApiSettings, callback: ApiResultCallback<Basket>) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val basket = createBasketWithProductFromSettings(settings)
            callback.onSuccess(basket)
        } catch (e: Exception) {
            callback.onError(e)
        }
    }
}

@ExperimentalSerializationApi
suspend fun OloApiClient.createBasketWithProductFromSettings(settings: IOloApiSettings): Basket {
    val vendorId = settings.restaurantId.value
    if (vendorId <= 0)
        throw IllegalStateException("Invalid vendor id: $vendorId")

    val productId = settings.productId.value
    if (productId <= 0)
        throw IllegalStateException("Invalid product id: $productId")

    val productQty = settings.productQty.value
    if (productQty <= 0)
        throw IllegalStateException("Invalid product quantity: $productQty")

    var basket = createBasket(vendorId)
    basket = setBasketHandoffMode("pickup", basket.id)
    basket = setBasketTimeModeAsap(basket.id)
    return addProductToBasket(productId, productQty, basket.id)
}