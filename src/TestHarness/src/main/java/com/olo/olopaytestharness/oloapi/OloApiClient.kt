// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi

import android.accounts.NetworkErrorException
import android.annotation.SuppressLint
import android.provider.Settings
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.olo.olopay.bootstrap.ApplicationProvider
import com.olo.olopaytestharness.oloapi.entities.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.lang.NullPointerException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@ExperimentalSerializationApi
class OloApiClient(
    baseUrl: String,
    private val apiKey: String
) {
    private val _baseUrl: String = if (baseUrl.startsWith("/")) baseUrl else "${baseUrl}/"
    private val _jsonFormatter = Json { ignoreUnknownKeys = true }

    suspend fun createBasket(restaurantId: Int): Basket {
        val url = createUrl("baskets/create")
        val data = mapOf(
            "vendorid" to restaurantId.toString(),
            "mode" to "orderahead"
        )

        val json = makeRequest(url, data)
        return _jsonFormatter.decodeFromString(json)
    }

    suspend fun setBasketHandoffMode(mode: String, basketId: String): Basket {
        val url = createUrl("baskets/${basketId}/deliverymode")
        val data = mapOf("deliverymode" to mode)

        val json = makeRequest(url, data, HttpMethod.PUT)
        return _jsonFormatter.decodeFromString(json)
    }

    suspend fun setBasketTimeModeAsap(basketId: String): Basket {
        val url = createUrl("baskets/${basketId}/timewanted")
        val json = makeRequest(url, httpMethod = HttpMethod.DELETE)
        return _jsonFormatter.decodeFromString(json)
    }

    suspend fun addProductToBasket(productId: Int, productQty: Int, basketId: String): Basket {
        val url = createUrl("baskets/${basketId}/products")
        val data = mapOf(
            "productid" to productId.toString(),
            "quantity" to productQty.toString()
        )

        val json = makeRequest(url, data)
        return _jsonFormatter.decodeFromString(json)
    }

    suspend fun availableBillingAccounts(authToken: String, basketId: String): BillingAccounts {
        val parameters = mapOf(
            "basket" to basketId
        )

        val url = createUrl("users/$authToken/billingAccounts", parameters)

        val json = makeRequest(url, httpMethod = HttpMethod.GET)
        return _jsonFormatter.decodeFromString(json)
    }

    suspend fun submitBasket(paymentType: PaymentType, basketId: String, user: User, billingId: Long?): Order {
        val url = createUrl("baskets/${basketId}/submit")

        // Base shared 'data' keys
        val data = mutableMapOf(
            "usertype" to (if(user.authToken != null) "user" else "guest"),
            "saveonfile" to false.toString(),
            "streetaddress" to "26 Broadway",
            "city" to "NYC",
            "state" to "NY",
            "contactnumber" to "5555558901",
        )

        val userData = if(!user.authToken.isNullOrEmpty()) {
            mapOf(
                "authtoken" to user.authToken,
            )
        } else {
            mapOf(
                "firstname" to user.firstName,
                "lastname" to user.lastName,
                "emailaddress" to user.emailAddress,
                "guestoptin" to "false",
            )
        }

        data.putAll(userData)

        val paymentData = if(paymentType.paymentMethod != null) {
            val paymentMethod = paymentType.paymentMethod
            val paymentMethodData = mutableMapOf(
                "billingmethod" to if(paymentMethod.isGooglePay) "digitalwallet" else "creditcardtoken",
                "expiryyear" to (if (paymentMethod.expirationYear != null) paymentMethod.expirationYear.toString() else ""),
                "expirymonth" to (if (paymentMethod.expirationMonth != null) paymentMethod.expirationMonth.toString() else ""),
                "token" to (paymentMethod.id ?: ""),
                "cardtype" to (if (paymentMethod.cardType != null) paymentMethod.cardType!!.description else ""),
                "cardlastfour" to (paymentMethod.last4 ?: ""),
                "zip" to (paymentMethod.postalCode ?: ""),
                "country" to (paymentMethod.countryCode ?: "US")
            )

            if (billingId != null && paymentMethod.isGooglePay) {
                paymentMethodData["billingschemeid"] = billingId.toString()
            }

            paymentMethodData
        } else {
            // CvvToken
            mapOf(
                "billingmethod" to "billingaccount",
                "cvv" to paymentType.cvvToken?.id!!,
                "billingaccountid" to billingId.toString(),
            )
        }

        data.putAll(paymentData)

        var responseString = makeRequest(url, data)

        return _jsonFormatter.decodeFromString(responseString)
    }

    suspend fun login(email: String, password: String): LoggedInUser {
        val url = createUrl("users/authenticate")
        val data = mutableMapOf(
            "login" to email,
            "password" to password,
        )

        val json = makeRequest(url, data)
        return _jsonFormatter.decodeFromString(json)

    }

    suspend fun logout(token: String) {
        val url = createUrl("users/$token")

        makeRequest(url, httpMethod = HttpMethod.DELETE)
    }

    private suspend fun makeRequest(
        apiUrl: String,
        data: Map<String, String>? = null,
        httpMethod: HttpMethod = HttpMethod.POST
    ): String = suspendCoroutine { continuation ->
        val jsonObjectRequest = OloStringRequest(httpMethod, apiUrl, data,
            { response ->
                if (response != null)
                    continuation.resume(response.toString())
                else
                    continuation.resumeWithException(NullPointerException("Unexpected null reference returned"))
            },
            { error ->
                if (error.networkResponse.data != null) {
                    val message = String(error.networkResponse.data)
                    continuation.resumeWithException(NetworkErrorException(message, error))
                } else {
                    continuation.resumeWithException(error)
                }
            }
        )

        jsonObjectRequest.retryPolicy =
            DefaultRetryPolicy(10000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        VolleyClient.instance.addToQueue(jsonObjectRequest)
    }

    private fun createUrl(url: String, parameters: Map<String, String>? = null) : String {
        val newUrl = url.trim('/')

        val params = parameters?.toMutableMap() ?: mutableMapOf()
        params["key"] = apiKey

        val queryString = createQueryString(params)
        return "$_baseUrl$newUrl?$queryString"
    }

    private fun createQueryString(parameters: Map<String, String>): String {
        return parameters.map { "${it.key}=${it.value}" }.joinToString("&")
    }

    class OloStringRequest(
        private val method: HttpMethod,
        url: String?,
        private val data: Map<String, String>?,
        listener: Response.Listener<JSONObject>?,
        errorListener: Response.ErrorListener?
    ) : JsonObjectRequest(method.toVolleyMethod(), url, if (data == null) null else JSONObject(data), listener, errorListener) {
        @SuppressLint("HardwareIds")
        override fun getHeaders(): MutableMap<String, String> {
            val headers = mutableMapOf<String, String>()

            if (method == HttpMethod.PUT || method == HttpMethod.DELETE) {
                headers["X-HTTP-Method-Override"] = method.toString()
            }

            headers["User-Agent"] = "OloPaySDKTestHarness-Android/1.0"
            headers["Accept"] = "application/json"

            val deviceId = Settings.Secure.getString(ApplicationProvider.currentApplication?.contentResolver, Settings.Secure.ANDROID_ID)
            if (deviceId != null)
                headers["X-Device-Id"] = deviceId

            return headers
        }

        override fun getParams(): MutableMap<String, String> {
            return data?.toMutableMap() ?: mutableMapOf()
        }

        override fun getBodyContentType(): String {
            if (method == HttpMethod.POST && data != null) {
                return "application/json"
            }

            return super.getBodyContentType()
        }
    }
}