// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.olo.olopay.api.ApiResultCallback
import com.olo.olopay.api.IOloPayApiInitializer
import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.api.OloPayApiInitializer
import com.olo.olopay.data.*
import com.olo.olopay.exceptions.CardException
import com.olo.olopay.exceptions.InvalidRequestException
import com.olo.olopay.exceptions.OloPayException
import com.olo.olopay.internal.data.Storage
import com.olo.olopay.testhelpers.BooleanWrapper
import com.olo.olopay.testhelpers.CvvTokenParamsHelpers
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
    fun createPaymentMethod_apiInitialized_invalidTestPublishableKey_updatesTestPublishableKey() {
        runBlocking {
            // Invalidate publishable keys for testing
            Storage(testContext).testPublishableKey = "Foobar"
            Storage(testContext).productionPublishableKey = null

            OloPayApiInitializer().setup(testContext, OloPayEnvironment.Test)
            val params = PaymentMethodParamsHelper.createValid()

            try{
                OloPayAPI().createPaymentMethod(testContext, params)
            } catch (e: java.lang.Exception){
                fail("createPaymentMethod should have succeeded - check other test failures for specific case")
            }

            assertNotEquals("Foobar", Storage(testContext).testPublishableKey)
            assertNull(Storage(testContext).productionPublishableKey)
        }
    }

    @Test
    fun createCvvUpdateToken_apiInitialized_invalidTestPublishableKey_updatesTestPublishableKey() {
        runBlocking {
            // Invalidate publishable keys for testing
            Storage(testContext).testPublishableKey = "Foobar"
            Storage(testContext).productionPublishableKey = null

            OloPayApiInitializer().setup(testContext, OloPayEnvironment.Test)
            val params = CvvTokenParamsHelpers.createValid()

            try{
                OloPayAPI().createCvvUpdateToken(testContext, params)
            } catch (e: java.lang.Exception){
                fail("createCvvUpdateToken should have succeeded - check other test failures for specific case")
            }

            assertNotEquals("Foobar", Storage(testContext).testPublishableKey)
            assertNull(Storage(testContext).productionPublishableKey)
        }
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
            OloPayApiInitializer().setup(context)

            val callback = object : ApiResultCallback<IPaymentMethod?> {
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
        assertEquals(PaymentMethodParamsHelper.ValidExpYear, paymentMethod.expirationYear)
        assertEquals(PaymentMethodParamsHelper.ValidExpMonth, paymentMethod.expirationMonth)
        assertEquals(PaymentMethodParamsHelper.ValidPostalCode, paymentMethod.postalCode)
        assertFalse(paymentMethod.isGooglePay)
    }

    @Test
    fun createCvvUpdateToken_apiInitialized_cvvParamsValid_returnsValidCvvUpdateToken() {
        val params = CvvTokenParamsHelpers.createValid()
        val cvvUpdateToken = submitCvvValue(params)

        assertNotEquals("", cvvUpdateToken.id)
    }

    @Test
    fun createPaymentMethodWithCallback_apiInitialized_paymentParamsValid_returnsValidPaymentMethod() {
        val params = PaymentMethodParamsHelper.createValid()
        val onSuccessCalled = BooleanWrapper(false)
        var paymentMethod: IPaymentMethod? = null

        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            OloPayApiInitializer().setup(context, OloPayEnvironment.Test)

            val callback = object : ApiResultCallback<IPaymentMethod?> {
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
            assertEquals(PaymentMethodParamsHelper.ValidExpYear, paymentMethod!!.expirationYear)
            assertEquals(PaymentMethodParamsHelper.ValidExpMonth, paymentMethod!!.expirationMonth)
            assertEquals(PaymentMethodParamsHelper.ValidPostalCode, paymentMethod!!.postalCode)
            assertFalse(paymentMethod!!.isGooglePay)
        }
    }

    @Test
    fun createCvvUpdateTokenWithCallback_apiInitialized_paymentParamsValid_returnsValidCvvUpdateToken() {
        val onSuccessCalled = BooleanWrapper(false)
        var cvvUpdateToken: ICvvUpdateToken? = null
        val params = CvvTokenParamsHelpers.createValid()

        val callback = object : ApiResultCallback<ICvvUpdateToken?> {
            override fun onSuccess(result: ICvvUpdateToken?) {
                cvvUpdateToken = result
                onSuccessCalled.value = true
            }

            override fun onError(e: Exception) {
                fail("onError should not be called")
            }
        }

        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            OloPayApiInitializer().setup(context, OloPayEnvironment.Test)
            OloPayAPI().createCvvUpdateToken(context, params, callback)

            waitForCondition(onSuccessCalled)
        }

        assertTrue(onSuccessCalled.value)
        assertNotNull(cvvUpdateToken)
        assertNotEquals("", cvvUpdateToken!!.id)
    }

    @Test
    fun createPaymentMethod_apiInitialized_paymentParamsInvalid_throwsException() {
        val params = PaymentMethodParamsHelper.createWithInvalidParams()
        try {
            submitPaymentMethod(params)
        } catch (e: CardException) {
            // Olo Pay controls the error message for this flow so we need to ensure we're testing the
            // the right flow/exception
            assertEquals(CardErrorType.UnknownCardError, e.type)
            assertEquals("Your card details are invalid", e.message)
            return
        } catch (e: Exception) {
            fail("Only CardException should be thrown")
            return
        }

        fail("CardException not thrown")
    }

    @Test
    fun createCvvUpdateToken_apiInitialized_cvvParamsInvalid_throwsException() {
        val params = CvvTokenParamsHelpers.createIncorrectParamsType()
        try {
            submitCvvValue(params)
        } catch (e: InvalidRequestException) {
            // Olo Pay controls the error message for this flow so we need to ensure we're testing the
            // the right flow/exception
            assertEquals("Params must be of type ICvvTokenParams", e.message)
            return
        } catch (e: Exception) {
            fail("Only InvalidRequestException should be thrown")
            return
        }

        fail("InvalidRequestException not thrown")
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
    fun createPaymentMethod_apiInitialized_paymentParamsInvalidCvv_throwsException() {
        val params = PaymentMethodParamsHelper.createWithInvalidCvv()
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
    fun createCvvUpdateToken_apiInitialized_invalidCvvParamsTooFewDigits_throwsException() {
        val params = CvvTokenParamsHelpers.createInvalidWithTooFewDigits()
        try {
            submitCvvValue(params)
        } catch (e: CardException) {
            // Olo Pay controls the error message for this flow so we need to ensure we're testing the
            // the right flow/exception
            assertEquals(CardErrorType.InvalidCVV, e.type)
            return
        } catch (e: Exception) {
            fail("Only CardException should be thrown")
            return
        }

        fail("CardException not thrown")
    }

    @Test
    fun createCvvUpdateToken_apiInitialized_invalidCvvParamsTooManyDigits_throwsException() {
        val params = CvvTokenParamsHelpers.createInvalidWithTooManyDigits()
        try {
            submitCvvValue(params)
        } catch (e: CardException) {
            // Olo Pay controls the error message for this flow so we need to ensure we're testing the
            // the right flow/exception
            assertEquals(CardErrorType.InvalidCVV, e.type)
            return
        } catch (e: Exception) {
            fail("Only CardException should be thrown")
            return
        }

        fail("CardException not thrown")
    }

    @Test
    fun createCvvUpdateToken_apiInitialized_invalidCvvParamsWithCharacters_throwsException() {
        val params = CvvTokenParamsHelpers.createInvalidWithCharacters()
        try {
            submitCvvValue(params)
        } catch (e: CardException) {
            // Olo Pay controls the error message for this flow so we need to ensure we're testing the
            // the right flow/exception
            assertEquals(CardErrorType.InvalidCVV, e.type)
            return
        } catch (e: Exception) {
            fail("Only CardException should be thrown")
            return
        }

        fail("CardException not thrown")
    }

    @Test
    fun createCvvUpdateToken_apiInitialized_cvvParamsInvalidEmptyCvv_throwsException() {
        val params = CvvTokenParamsHelpers.createInvalidWithEmptyCvv()
        try {
            submitCvvValue(params)
        } catch (e: CardException) {
            // Olo Pay controls the error message for this flow so we need to ensure we're testing the
            // the right flow/exception
            assertEquals(CardErrorType.InvalidCVV, e.type)
            assertEquals("Your card's security code is missing", e.message)
            return
        } catch (e: Exception) {
            fail("Only CardException should be thrown")
            return
        }

        fail("CardException not thrown")
    }

    @Test
    fun updateTestPublishableKey_storesKey() {
        runBlocking {
            IOloPayApiInitializer.environment = OloPayEnvironment.Test
            Storage(testContext).testPublishableKey = "" //Clear key to ensure it's not set prior to updating publishable key

            OloPayAPI.updatePublishableKey(testContext)
            assertFalse(Storage(testContext).testPublishableKey.isNullOrEmpty())
        }
    }

    @Test
    fun createPaymentMethod_apiNotInitialized_invalidTestPublishableKey_returnsSetupException() {
        runBlocking {
            val params = PaymentMethodParamsHelper.createValid()

            try{
                OloPayAPI().createPaymentMethod(testContext, params)
            } catch (e: java.lang.Exception) {
                assertTrue(e is OloPayException)
                assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), e.message)
                return@runBlocking
            }

            fail("createPaymentMethod should have thrown an OloPayException since OloPayAPI was not initialized")
        }
    }

    @Test
    fun createCvvUpdateToken_apiNotInitialized_invalidTestPublishableKey_returnsSetupException() {
        runBlocking {
            val params = CvvTokenParamsHelpers.createValid()

            try{
                OloPayAPI().createCvvUpdateToken(testContext, params)
            } catch (e: java.lang.Exception) {
                assertTrue(e is OloPayException)
                assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), e.message)
                return@runBlocking
            }

            fail("createCvvUpdateToken should have thrown an OloPayException since OloPayAPI was not initialized")
        }
    }

    @Test
    fun createPaymentMethodWithCallback_apiNotInitialized_paymentParamsValid_returnsSetupException() {
        val enteredCallback = BooleanWrapper(false)
        var oloPayException: OloPayException? = null
        val params = PaymentMethodParamsHelper.createValid()


        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext

            val callback = object : ApiResultCallback<IPaymentMethod?> {
                override fun onSuccess(result: IPaymentMethod?) {
                    fail("onSuccess should not be called")
                }

                override fun onError(e: Exception) {
                    enteredCallback.value = true
                    oloPayException = e as? OloPayException
                }
            }

            OloPayAPI().createPaymentMethod(context, params, callback)
            waitForCondition(enteredCallback)

            assertNotNull(oloPayException)
            assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), oloPayException!!.message)
        }
    }

    @Test
    fun createCvvUpdateTokenWithCallback_apiNotInitialized_cvvParamsValid_returnsSetupException() {
        val enteredCallback = BooleanWrapper(false)
        var exception: Exception? = null
        var onSuccessCalled = false
        var cvvUpdateToken: ICvvUpdateToken? = null
        val params = CvvTokenParamsHelpers.createValid()

        val callback = object : ApiResultCallback<ICvvUpdateToken?> {
            override fun onSuccess(result: ICvvUpdateToken?) {
                cvvUpdateToken = result
                onSuccessCalled = true
            }

            override fun onError(e: Exception) {
                enteredCallback.value = true
                exception = e
            }
        }

        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            OloPayAPI().createCvvUpdateToken(context, params, callback)
            waitForCondition(enteredCallback)
        }

        assertFalse(onSuccessCalled)

        val oloPayException = exception as? OloPayException
        assertNotNull(oloPayException)
        assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), oloPayException!!.message)
        assertNull(cvvUpdateToken)
    }

    @Test
    fun createPaymentMethod_apiNotInitialized_paymentParamsInvalid_returnsSetupException() {
        val params = PaymentMethodParamsHelper.createWithInvalidParams()
        try {
            submitPaymentMethod(params, false)
        } catch (e: java.lang.Exception){
            assertTrue(e is OloPayException)
            assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), e.message)
            return
        }
        fail("submitPaymentMethod should have thrown an OloPayException since OloPayAPI was not initialized")
    }

    @Test
    fun createCvvUpdateToken_apiNotInitialized_cvvParamsInvalid_returnsSetupException() {
        val params = CvvTokenParamsHelpers.createInvalidWithEmptyCvv()
        try {
            submitCvvValue(params, false)
        } catch (e: java.lang.Exception){
            assertTrue(e is OloPayException)
            assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), e.message)
            return
        }
        fail("submitCvvValue should have thrown an OloPayException since OloPayAPI was not initialized")
    }

    @Test
    fun createPaymentMethod_apiNotInitialized_paymentParamsInvalidNumber_returnsSetupException() {
        val params = PaymentMethodParamsHelper.createWithInvalidNumber()
        try {
            submitPaymentMethod(params, false)
        } catch (e: java.lang.Exception){
            assertTrue(e is OloPayException)
            assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), e.message)
            return
        }
        fail("submitPaymentMethod should have thrown an OloPayException since OloPayAPI was not initialized")
    }

    @Test
    fun createPaymentMethod_apiNotInitialized_paymentParamsInvalidYear_returnsSetupException() {
        val params = PaymentMethodParamsHelper.createWithInvalidYear()
        try {
            submitPaymentMethod(params, false)
        } catch (e: java.lang.Exception) {
            assertTrue(e is OloPayException)
            assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), e.message)
            return
        }
        fail("submitPaymentMethod should have thrown an OloPayException since OloPayAPI was not initialized")
    }

    @Test
    fun createPaymentMethod_apiNotInitialized_paymentParamsInvalidMonth_returnsSetupException() {
        val params = PaymentMethodParamsHelper.createWithInvalidMonth()
        try {
            submitPaymentMethod(params, false)
        } catch (e: java.lang.Exception) {
            assertTrue(e is OloPayException)
            assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), e.message)
            return
        }
        fail("submitPaymentMethod should have thrown an OloPayException since OloPayAPI was not initialized")
    }

    @Test
    fun createPaymentMethod_apiNotInitialized_paymentParamsInvalidCvv_returnsSetupException() {
        val params = PaymentMethodParamsHelper.createWithInvalidCvv()
        try {
            submitPaymentMethod(params, false)
        } catch (e: java.lang.Exception) {
            assertTrue(e is OloPayException)
            assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), e.message)
            return
        }
        fail("submitPaymentMethod should have thrown an OloPayException since OloPayAPI was not initialized")
    }

    @Test
    fun createPaymentMethod_apiNotInitialized_paymentParamsUnsupportedCardBrand_returnsSetupException() {
        val params = PaymentMethodParamsHelper.createWithUnsupportedCardBrand()
        try {
            submitPaymentMethod(params, false)
        } catch (e: java.lang.Exception) {
            assertTrue(e is OloPayException)
            assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), e.message)
            return
        }
        fail("submitPaymentMethod should have thrown an OloPayException since OloPayAPI was not initialized")
    }

    @Test
    fun createCvvUpdateToken_apiNotInitialized_invalidCvvParamsTooFewDigits_returnsSetupException() {
        val params = CvvTokenParamsHelpers.createInvalidWithTooFewDigits()
        try {
            submitCvvValue(params, false)
        } catch (e: java.lang.Exception){
            assertTrue(e is OloPayException)
            assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), e.message)
            return
        }
        fail("submitCvvValue should have thrown an OloPayException since OloPayAPI was not initialized")
    }

    @Test
    fun createCvvUpdateToken_apiNotInitialized_invalidCvvParamsTooManyDigits_returnsSetupException() {
        val params = CvvTokenParamsHelpers.createInvalidWithTooManyDigits()
        try {
            submitCvvValue(params, false)
        } catch (e: java.lang.Exception){
            assertTrue(e is OloPayException)
            assertEquals(testContext.getString(R.string.olopay_setup_not_called_error), e.message)
            return
        }
        fail("submitCvvValue should have thrown an OloPayException since OloPayAPI was not initialized")
    }

    private fun submitPaymentMethod(
        params: IPaymentMethodParams?,
        initializeSDK: Boolean = true,
    ): IPaymentMethod {
        var paymentMethod: IPaymentMethod? = null
        runBlocking {
            if (initializeSDK) {
                OloPayApiInitializer().setup(testContext, OloPayEnvironment.Test)
            }

            paymentMethod = OloPayAPI().createPaymentMethod(testContext, params)
        }

        return paymentMethod!!
    }

    private fun submitCvvValue(
        params: ICvvTokenParams,
        initializeSDK: Boolean = true,
    ): ICvvUpdateToken {
        var cvvUpdateToken: ICvvUpdateToken? = null
        runBlocking {
            if (initializeSDK) {
                OloPayApiInitializer().setup(testContext, OloPayEnvironment.Test)
            }
            cvvUpdateToken = OloPayAPI().createCvvUpdateToken(testContext, params)
        }
        return cvvUpdateToken!!
    }

    private val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
}