// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.testhelpers

import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopay.internal.data.PaymentMethodParams
import com.stripe.android.model.Address
import com.stripe.android.model.CardParams

import com.stripe.android.model.PaymentMethodCreateParams as StripePaymentMethodParams

class PaymentMethodParamsHelper {
    companion object {
        fun createValid() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(ValidCardNumber, ValidExpMonth, ValidExpYear, ValidCvc, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams)
        }

        fun createWithInvalidNumber() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(InvalidCardNumber, ValidExpMonth, ValidExpYear, ValidCvc, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams)
        }

        fun createWithInvalidYear() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(ValidCardNumber, ValidExpMonth, InvalidExpYear, ValidCvc, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams)
        }

        fun createWithInvalidMonth() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(ValidCardNumber, InvalidExpMonth, ValidExpYear, ValidCvc, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams)
        }

        fun createWithInvalidCvc() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(ValidCardNumber, ValidExpMonth, ValidExpYear, InvalidCvc, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams)
        }

        fun createWithUnsupportedCardBrand() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(DiningClubCardNumber, ValidExpMonth, ValidExpYear, ValidCvc, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams)
        }

        private const val ValidCardNumber = "4242424242424242"
        private const val InvalidCardNumber = "1234567890123456"
        private const val DiningClubCardNumber = "3056930009020004"
        const val ValidExpYear = 2025
        private const val InvalidExpYear = 2020
        const val ValidExpMonth = 12
        private const val InvalidExpMonth = 24
        private const val ValidCvc = "234"
        private const val InvalidCvc = "12"
        const val ValidPostalCode = "10004"
    }
}