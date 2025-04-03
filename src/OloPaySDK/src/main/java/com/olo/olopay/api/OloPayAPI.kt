// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.api

import android.content.Context
import com.olo.olopay.R
import com.olo.olopay.bootstrap.ApplicationProvider
import com.olo.olopay.data.*
import com.olo.olopay.exceptions.*
import com.olo.olopay.internal.data.*
import com.olo.olopay.internal.data.CvvUpdateToken
import com.olo.olopay.internal.data.PaymentMethod
import com.olo.olopay.internal.data.PaymentMethodParams
import com.olo.olopay.internal.data.Storage
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.createPaymentMethod
import com.stripe.android.core.exception.APIConnectionException as StripeApiConnectionException
import com.stripe.android.core.exception.APIException as StripeApiException
import com.stripe.android.core.exception.AuthenticationException as StripeAuthenticationException
import com.stripe.android.core.exception.InvalidRequestException as StripeInvalidRequestException
import com.stripe.android.exception.CardException as StripeCardException
import com.stripe.android.core.exception.RateLimitException as StripeRateLimitException
import com.stripe.android.core.exception.StripeException
import com.stripe.android.createCvcUpdateToken
import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.JSONTokener


/**
 * Represents the Olo Pay API and functionality related to it
 * <hr class="spacer">
 *
 * #### Important:
 * Prior to calling methods in this class be sure to initialize the SDK by calling [OloPayApiInitializer.setup]
 *
 * @see OloPayApiInitializer Use this class to initialize the Olo Pay SDK
 *
 * @constructor Creates a new [OloPayAPI] instance
 */
class OloPayAPI : IOloPayAPI {
    /**
     * Creates an [IPaymentMethod] instance with the provided parameters and returns it via [callback].
     * <hr class="spacer">
     *
     * #### Important:
     * This version of the method is provided mainly as convenience for Java developers. Kotlin
     * developers should generally use the suspend version instead
     *
     * @param context Activity or Application context
     * @param params The parameters used to create the payment method
     * @param callback The callback used to return the result.
     *
     * **_Note:_** _This callback returns on a background thread_
     */
    override fun createPaymentMethod(context: Context, params: IPaymentMethodParams?, callback: ApiResultCallback<IPaymentMethod?>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val paymentMethod = createPaymentMethod(context, params, true)
                callback.onSuccess(paymentMethod)
            } catch (e: OloPayException) {
                callback.onError(e)
            }
        }
    }

    /**
     * Creates an [IPaymentMethod] instance with the provided parameters
     * <hr class="spacer">
     * @param context Activity or Application context
     * @param params The parameters used to create the payment method
     * @return An [IPaymentMethod] instance, which can then be used to submit a basket to Olo's Ordering API
     * @throws ApiException
     * @throws CardException
     * @throws InvalidRequestException
     * @throws ApiConnectionException
     * @throws RateLimitException
     * @throws OloPayException
     */
    override suspend fun createPaymentMethod(context: Context, params: IPaymentMethodParams?): IPaymentMethod {
        return createPaymentMethod(context, params, true)
    }

    private suspend fun createPaymentMethod(context: Context, params: IPaymentMethodParams?, firstTry: Boolean): PaymentMethod {
        try {
            val stripe = Stripe(context, PaymentConfiguration.getInstance(context).publishableKey)

            if (params == null || params !is PaymentMethodParams) {
                throw CardException(CardErrorType.UnknownCardError, ApplicationProvider.currentApplication?.getString(
                    R.string.olopay_invalid_card_details
                ))
            }

            val paymentMethod = PaymentMethod(stripe.createPaymentMethod(params.params), params.googlePayData, params.googlePayConfig)

            if (paymentMethod.cardType == CardBrand.Unknown || paymentMethod.cardType == CardBrand.Unsupported) {
                val errorMessage =
                    if (paymentMethod.cardType == CardBrand.Unsupported)
                        ApplicationProvider.currentApplication?.getString(R.string.olopay_unsupported_card_type_error)
                    else
                        ApplicationProvider.currentApplication?.getString(R.string.olopay_invalid_card_details)

                throw CardException(CardErrorType.InvalidNumber, errorMessage)
            }

            return paymentMethod
        } catch (e: StripeAuthenticationException) {
            if (firstTry) {
                updatePublishableKey(context)
                return createPaymentMethod(context, params, false)
            }

            throw ApiException(e)
        } catch (e: StripeCardException) {
            throw CardException(e)
        } catch (e: StripeInvalidRequestException) {
            throw InvalidRequestException(e)
        } catch (e: StripeApiConnectionException) {
            throw ApiConnectionException(e)
        } catch (e: StripeRateLimitException) {
            throw RateLimitException(e)
        } catch (e: StripeApiException) {
            throw ApiException(e)
        } catch (e: StripeException) {
            throw OloPayException(e, null)
        } catch (e: java.lang.IllegalStateException){
            throw OloPayException(null, context.getString(R.string.olopay_setup_not_called_error))
        }
    }

    /**
     * Creates an [ICvvUpdateToken] instance with the provided parameters and returns it via [callback].
     * <hr class="spacer">
     *
     * #### Important:
     * This version of the method is provided mainly as convenience for Java developers. Kotlin
     * developers should generally use the suspend version instead
     *
     * @param context Activity or Application context
     * @param params The parameters used to create the CVV validation token
     * @param callback The callback used to return the result.
     *
     * **_Note:_** _This callback returns on a background thread_
     */
    override fun createCvvUpdateToken(context: Context, params: ICvvTokenParams, callback: ApiResultCallback<ICvvUpdateToken?>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cvvUpdateToken = createCvvUpdateToken(context,params, true)
                callback.onSuccess(cvvUpdateToken)
            } catch (e: OloPayException) {
                callback.onError(e)
            }
        }
    }

    /**
     * Creates an [ICvvUpdateToken] instance with the provided parameters
     * <hr class="spacer">
     * @param context Activity or Application context
     * @param params The parameters used to create the CVV validation token
     * @return An [ICvvUpdateToken] instance, which can then be used revalidate a previously used credit/debit card on file
     * @throws ApiException
     * @throws CardException
     * @throws InvalidRequestException
     * @throws ApiConnectionException
     * @throws RateLimitException
     * @throws OloPayException
     */
    override suspend fun createCvvUpdateToken(context: Context, params: ICvvTokenParams): ICvvUpdateToken {
        return createCvvUpdateToken(context, params, true)
    }

    private suspend fun createCvvUpdateToken(context: Context, params:ICvvTokenParams, firstTry: Boolean): CvvUpdateToken {
        try {
            val stripe = Stripe(context, PaymentConfiguration.getInstance(context).publishableKey)

            if (params !is CvvTokenParams) {
                throw InvalidRequestException(ApplicationProvider.currentApplication?.getString(
                    R.string.olopay_invalid_cvv_params_type
                ))
            }

            if(params.cvvValue.isEmpty()){
                throw CardException(CardErrorType.InvalidCVV, ApplicationProvider.currentApplication?.getString(
                    R.string.olopay_empty_cvv_error
                ))
            }

            return CvvUpdateToken(stripe.createCvcUpdateToken(params.cvvValue))
        } catch (e: StripeAuthenticationException) {
            if (firstTry) {
                updatePublishableKey(context)
                return createCvvUpdateToken(context, params, false)
            }

            throw ApiException(e)
        } catch (e: StripeCardException) {
            throw CardException(e)
        } catch (e: StripeInvalidRequestException) {
            throw InvalidRequestException(e)
        } catch (e: StripeApiConnectionException) {
            throw ApiConnectionException(e)
        } catch (e: StripeRateLimitException) {
            throw RateLimitException(e)
        } catch (e: StripeApiException) {
            throw ApiException(e)
        } catch (e: StripeException) {
            throw OloPayException(e, null)
        } catch (e: java.lang.IllegalStateException){
            throw OloPayException(null, context.getString(R.string.olopay_setup_not_called_error))
        }
    }

    /** @suppress */
    companion object {
        private const val JSON_TOKEN_KEY = "key"

        internal suspend fun updatePublishableKey(context: Context) {
            withContext(Dispatchers.IO) {
                runCatching {
                    val url = context.getString(IOloPayApiInitializer.publishableKeyResource)

                    val response = java.net.URL(url).readText()
                    val json = JSONTokener(response).nextValue() as JSONObject

                    val publishableKey = json.getString(JSON_TOKEN_KEY)

                    Storage(context).setPublishableKey(IOloPayApiInitializer.environment, publishableKey)

                    PaymentConfiguration.init(context, publishableKey)
                }
            }
        }
    }
}