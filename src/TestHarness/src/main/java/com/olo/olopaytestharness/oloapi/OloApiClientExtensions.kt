// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi

import com.olo.olopay.api.ApiResultCallback
import com.olo.olopay.exceptions.OloPayException
import com.olo.olopaytestharness.models.IOloApiSettings
import com.olo.olopaytestharness.models.IUserSettings
import com.olo.olopaytestharness.oloapi.entities.*
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
suspend fun OloApiClient.submitBasketFromSettings(apiSettings: IOloApiSettings, userSettings: IUserSettings, paymentType: PaymentType, basketId: String) : Order {
    if(userSettings.userEmail.value.isEmpty()){
        throw OloPayException("Unable to send basket: User email not set")
    }
    if(userSettings.userPassword.value.isEmpty() && userSettings.useLoggedInUser.value){
        throw OloPayException("Unable to send basket: User password not set")
    }

    val user: User?
    val billingAccounts: BillingAccounts?
    val billingId: Long?

    if (paymentType.paymentMethod != null){
        // PaymentMethod
        billingId = if(paymentType.paymentMethod.isGooglePay) apiSettings.googlePayBillingSchemeId.value.toLong() else null
        user = if(userSettings.useLoggedInUser.value){
            // Logged in user
            login(userSettings.userEmail.value, userSettings.userPassword.value).user
        } else {
            // Guest user
            User(null, userSettings.userEmail.value, "Ron", "Idaho")
        }
    } else {
        // CVV Token
        if(!userSettings.useLoggedInUser.value){
            throw OloPayException("CVV revalidation requires a logged in user")
        }

        // Logged in user is required with CVV order submission
        user = login(userSettings.userEmail.value, userSettings.userPassword.value).user
        billingAccounts = this.availableBillingAccounts(user.authToken!!, basketId)
        billingId = userSettings.savedCardBillingId.value.toLong()

        val validBillingAccounts =
            billingAccounts.billingAccounts?.filter { it.accountId.toLong() != billingId }
        if (validBillingAccounts?.isEmpty() == true) {
            throw OloPayException("Unable to submit basket - Saved billing account id not valid")
        }
    }

    val order = submitBasket(paymentType, basketId, user, billingId)

    // In a real-world application, you would not want to log the user out after creating an order
    user.authToken?.let {
        logout(it)
    }

    return order
}

@ExperimentalSerializationApi
fun OloApiClient.submitBasketFromSettings(apiSettings: IOloApiSettings, userSettings: IUserSettings, paymentType: PaymentType, basketId: String, callback: ApiResultCallback<Order>) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val order = submitBasketFromSettings(apiSettings, userSettings, paymentType, basketId)

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
