// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.olo.olopay.api.ApiResultCallback
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.api.OloPayApiInitializer
import com.olo.olopay.data.*
import com.olo.olopay.exceptions.CardException
import com.olo.olopay.internal.data.Storage
import com.olo.olopay.testhelpers.BooleanWrapper
import com.olo.olopay.testhelpers.PaymentMethodParamsHelper
import com.olo.olopay.testhelpers.waitForCondition
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class OloPayApiTests {
    @Test
    fun createPaymentMethod_apiInitialized_invalidPublishableKey_updatesPublishableKey() {
        Storage(testContext).publishableKey = "Foobar"

        val params = PaymentMethodParamsHelper.createValid()
        submitPaymentMethod(params)
        assertNotEquals("Foobar", Storage(testContext).publishableKey)
    }

    @Test(expected = CardException::class)
    fun createPaymentMethod_apiInitialized_paymentParamsNull_throwsCardException() {
        submitPaymentMethod(null)
    }

    @Test
    fun createPaymentMethodWithCallback_apiInitialized_paymentParamsNull_returnsCardException() {
        val enteredCallback = BooleanWrapper(false)
        var cardException = false

        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            OloPayApiInitializer().setup(context);

            val callback = object: ApiResultCallback<IPaymentMethod?> {
                override fun onSuccess(result: IPaymentMethod?) {
                    fail("onSuccess should not be called")
                    enteredCallback.value = true
                }

                override fun onError(e: Exception) {
                    enteredCallback.value = true
                    cardException = e is CardException
                }
            }

            OloPayAPI().createPaymentMethod(context, null, callback)
            waitForCondition(enteredCallback)

            assertTrue(enteredCallback.value)
            assertTrue(cardException)
        }
    }

    @Test
    fun createPaymentMethod_apiInitialized_paymentParamsValid_returnsValidPaymentMethod() {
        val params = PaymentMethodParamsHelper.createValid()
        val paymentMethod = submitPaymentMethod(params)

        assertEquals("4242", paymentMethod.last4)
        assertEquals(PaymentMethodParamsHelper.ValidExpYear, paymentMethod.expirationYear, )
        assertEquals(PaymentMethodParamsHelper.ValidExpMonth, paymentMethod.expirationMonth)
        assertEquals(PaymentMethodParamsHelper.ValidPostalCode, paymentMethod.postalCode)
        assertFalse(paymentMethod.isGooglePay)
    }

    @Test
    fun createPaymentMethodWithCallback_apiInitialized_paymentParamsValid_returnsValidPaymentMethod() {
        val params = PaymentMethodParamsHelper.createValid()
        val onSuccessCalled = BooleanWrapper(false)
        var paymentMethod: IPaymentMethod? = null

        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            OloPayApiInitializer().setup(context);

            val callback = object: ApiResultCallback<IPaymentMethod?> {
                override fun onSuccess(result: IPaymentMethod?) {
                    paymentMethod = result
                    onSuccessCalled.value = true
                }

                override fun onError(e: Exception) {
                    fail("onError should not be called")
                }
            }

            OloPayAPI().createPaymentMethod(context, params, callback)
            waitForCondition(onSuccessCalled)

            assertTrue(onSuccessCalled.value)
            assertNotNull(paymentMethod)

            assertEquals("4242", paymentMethod!!.last4)
            assertEquals(PaymentMethodParamsHelper.ValidExpYear, paymentMethod!!.expirationYear, )
            assertEquals(PaymentMethodParamsHelper.ValidExpMonth, paymentMethod!!.expirationMonth)
            assertEquals(PaymentMethodParamsHelper.ValidPostalCode, paymentMethod!!.postalCode)
            assertFalse(paymentMethod!!.isGooglePay)
        }
    }

    @Test(expected = CardException::class)
    fun createPaymentMethod_apiInitialized_paymentParamsInvalidNumber_throwsException() {
        val params = PaymentMethodParamsHelper.createWithInvalidNumber()
        submitPaymentMethod(params)
    }

    @Test(expected = CardException::class)
    fun createPaymentMethod_apiInitialized_paymentParamsInvalidYear_throwsException() {
        val params = PaymentMethodParamsHelper.createWithInvalidYear()
        submitPaymentMethod(params)
    }

    @Test(expected = CardException::class)
    fun createPaymentMethod_apiInitialized_paymentParamsInvalidMonth_throwsException() {
        val params = PaymentMethodParamsHelper.createWithInvalidMonth()
        submitPaymentMethod(params)
    }

    @Test(expected = CardException::class)
    fun createPaymentMethod_apiInitialized_paymentParamsInvalidCvc_throwsException() {
        val params = PaymentMethodParamsHelper.createWithInvalidCvc()
        submitPaymentMethod(params)
    }

    @Test
    fun createPaymentMethod_apiInitialized_paymentParamsUnsupportedCardBrand_throwsException() {
        val params = PaymentMethodParamsHelper.createWithUnsupportedCardBrand()
        try {
            submitPaymentMethod(params)
        } catch (e: CardException) {
            // Olo Pay controls the error message for this flow so we need to ensure we're testing the
            // the right flow/exception
            assertEquals(CardErrorType.InvalidNumber, e.type)
            assertEquals("Your card type is not supported", e.message)
            return
        } catch (e: Exception) {
            fail("Only CardException should be thrown")
            return
        }

        fail("CardException not thrown")
    }

    @Test
    fun updatePublishableKey_storesKey() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            Storage(context).publishableKey = "" //Clear key to ensure it's not set prior to updating publishable key

            OloPayAPI.updatePublishableKey(context)
            assertFalse(Storage(context).publishableKey.isNullOrEmpty())
        }
    }

    private fun submitPaymentMethod(params: IPaymentMethodParams?, initializeSDK: Boolean = true) : IPaymentMethod {
        var paymentMethod: IPaymentMethod? = null
        runBlocking {
            if (initializeSDK) {
                var setupParams = SetupParameters(OloPayEnvironment.Test, true)
                OloPayApiInitializer().setup(testContext, setupParams);
            }

            paymentMethod = OloPayAPI().createPaymentMethod(testContext, params)
        }

        return paymentMethod!!
    }

    private val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
}