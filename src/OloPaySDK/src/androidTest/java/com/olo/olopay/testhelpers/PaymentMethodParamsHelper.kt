// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.testhelpers

import com.olo.olopay.data.IPaymentMethodParams
import com.olo.olopay.internal.data.PaymentMethodParams
import com.olo.olopay.internal.data.PaymentMethodSource
import com.stripe.android.model.Address
import com.stripe.android.model.CardParams

import com.stripe.android.model.PaymentMethodCreateParams as StripePaymentMethodParams

class PaymentMethodParamsHelper {
    companion object {
        fun createValid() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(ValidCardNumber, ValidExpMonth, ValidExpYear, ValidCvv, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams, PaymentMethodSource.SingleLineInput)
        }

        fun createWithInvalidParams() : IPaymentMethodParams {
            return InvalidPaymentMethodParams()
        }

        fun createWithInvalidNumber() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(InvalidCardNumber, ValidExpMonth, ValidExpYear, ValidCvv, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams, PaymentMethodSource.SingleLineInput)
        }

        fun createWithInvalidYear() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(ValidCardNumber, ValidExpMonth, InvalidExpYear, ValidCvv, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams, PaymentMethodSource.SingleLineInput)
        }

        fun createWithInvalidMonth() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(ValidCardNumber, InvalidExpMonth, ValidExpYear, ValidCvv, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams, PaymentMethodSource.SingleLineInput)
        }

        fun createWithInvalidCvv() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(ValidCardNumber, ValidExpMonth, ValidExpYear, InValidCvv, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams, PaymentMethodSource.SingleLineInput)
        }

        fun createWithUnsupportedCardBrand() : IPaymentMethodParams {
            val address = Address(postalCode = ValidPostalCode)
            val cardParams = CardParams(DiningClubCardNumber, ValidExpMonth, ValidExpYear, ValidCvv, address = address)
            val stripePaymentMethodParams = StripePaymentMethodParams.createCard(cardParams)
            return PaymentMethodParams(stripePaymentMethodParams, PaymentMethodSource.SingleLineInput)
        }

        private class InvalidPaymentMethodParams: IPaymentMethodParams {}
        private const val ValidCardNumber = "4242424242424242"
        private const val InvalidCardNumber = "1234567890123456"
        private const val DiningClubCardNumber = "3056930009020004"
        const val ValidExpYear = 2025
        private const val InvalidExpYear = 2020
        const val ValidExpMonth = 12
        private const val InvalidExpMonth = 24
        private const val ValidCvv = "234"
        private const val InValidCvv = "12"
        const val ValidPostalCode = "10004"
    }
}