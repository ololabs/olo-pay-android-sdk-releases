// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.googlepay

import android.os.Build
import android.os.Parcelable
import com.olo.olopay.data.Address
import com.olo.olopay.data.CardBrand
import com.olo.olopay.internal.extensions.getOrDefault
import com.olo.olopay.internal.extensions.getOrNull
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import java.util.Base64

@Parcelize
/**
 * Class to represent Google Pay payment data
 *
 * Note: This class represents a merger of [PaymentData](https://developers.google.com/pay/api/android/reference/response-objects#PaymentData)
 * and [PaymentMethodData](https://developers.google.com/pay/api/android/reference/response-objects#PaymentMethodData)
 * response objects from the Google Pay API
 */
data class GooglePaymentData (
    /**
     * Encrypted token string from Google that represents the selected payment method.
     *
     * **_NOTE:_** This field is a Base-64 encoded version of the `token` property of Google's
     * [PaymentMethodTokenizationData](https://developers.google.com/pay/api/android/reference/response-objects#PaymentMethodTokenizationData)
     * response object
     */
    val token: String,

    /**
     * User-facing message to describe the payment method that funds this transaction.
     *
     * **_NOTE:_** This field maps to the `description` property of Google's
     * [PaymentMethodData](https://developers.google.com/pay/api/android/reference/response-objects#PaymentMethodData)
     * response object
     */
    val description: String,

    /**
     * The type of card used for this transaction.
     *
     * **_NOTE:_** This field is an enum representation of the `cardNetwork` property of Google's
     * [CardInfo](https://developers.google.com/pay/api/android/reference/response-objects#CardInfo)
     * response object
     */
    val cardType: CardBrand,

    /**
     * The details about the card. This is commonly the last four digits of the payment account number.
     *
     * **_NOTE:_** This field maps to the `cardDetails` property of Google's
     * [CardInfo](https://developers.google.com/pay/api/android/reference/response-objects#CardInfo)
     * response object
     */
    val cardDetails: String,

    /**
     * The last four digits of the card number, if provided by Google, otherwise an empty string.
     *
     * **_NOTE:_** This may often contain the same value as `cardDetails`. However Google does not
     * guarantee that `cardDetails` will _only_ contain the last four digits of the payment account
     * number, or that it will contain the last four digits at all. This property extracts the last
     * four digits from `cardDetails`.
     */
    val lastFour: String,

    /**
     * The billing address associated with the payment method, if Google Pay was configured to return
     * address data.
     *
     * **_NOTE:_** This field maps to the `billingAddress` property of Google's
     * [CardInfo](https://developers.google.com/pay/api/android/reference/response-objects#CardInfo)
     * response object
     */
    val billingAddress: Address?,

    /**
     * The email address associated with the transaction, if Google Pay was configured to require
     * an email address.
     *
     * **_NOTE:_** This field maps to the `email` property of Google's
     * [PaymentData](https://developers.google.com/pay/api/android/reference/response-objects#PaymentData)
     * response object
     */
    val email: String,

    /**
     * The name associated with the transaction
     */
    val name: String,

    /**
     * The phone number associated with the transaction
     */
    val phoneNumber: String
) : Parcelable {

    internal companion object {
        fun fromJson(googlePaymentData: JSONObject): GooglePaymentData {
            val paymentMethodData = googlePaymentData.getJSONObject(PAYMENT_METHOD_DATA_KEY)
            val cardInfoData = paymentMethodData.getJSONObject(INFO_KEY)

            // NOTE: We do not currently use the this token field in the Olo Pay SDK, but that may
            //       change in the future
            val tokenString = paymentMethodData
                .getJSONObject(TOKENIZATION_DATA_KEY)
                .getString(TOKEN_KEY)
                .let {
                    // IMPORTANT: This was tested with Olo's back-end to ensure it could properly be
                    // decoded from Base64. Any changes to this MUST be tested with the back-end to
                    // ensure it still works
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Base64.getUrlEncoder().encodeToString(JSONObject(it).toString().toByteArray())
                    } else {
                        ""
                    }
                }

            val cardDetails = cardInfoData.getOrDefault(CARD_DETAILS_KEY, "")
            val billingAddressJson = cardInfoData.getOrNull<JSONObject>(BILLING_ADDRESS_KEY)

            return GooglePaymentData(
                token = tokenString,
                description = paymentMethodData.getOrDefault(DESCRIPTION_KEY, ""),
                cardType = CardBrand.convertFrom(cardInfoData.getOrDefault(CARD_NETWORK_KEY, "")),
                cardDetails = cardDetails,
                lastFour = parseLastFour(cardDetails),
                billingAddress = Address.from(billingAddressJson),
                email = googlePaymentData.getOrDefault(EMAIL_KEY, ""),
                name = billingAddressJson?.getOrDefault(NAME_KEY, "") ?: "",
                phoneNumber = billingAddressJson?.getOrDefault(PHONE_NUMBER_KEY, "") ?: ""
            )
        }

        private fun parseLastFour(data: String): String {
            val digitMatches = Regex(LAST_FOUR_REGEX).findAll(data.trim())

            // First attempt to return the last found set of exactly 4 digits
            if (digitMatches.any { it.value.length == LAST_FOUR_DIGIT_COUNT }) {
                return digitMatches.last { it.value.length == LAST_FOUR_DIGIT_COUNT }.value
            }

            // Next, return the last four digits of a set of digits with a length greater than 4
            if (digitMatches.any{ it.value.length > LAST_FOUR_DIGIT_COUNT }) {
                return digitMatches.last().value.takeLast(LAST_FOUR_DIGIT_COUNT)
            }

            // If there are no strings with at least four digits, return empty string
            return ""
        }

        private const val NAME_KEY = "name"
        private const val PHONE_NUMBER_KEY = "phoneNumber"
        private const val EMAIL_KEY = "email"
        private const val PAYMENT_METHOD_DATA_KEY = "paymentMethodData"
        private const val DESCRIPTION_KEY = "description"
        private const val INFO_KEY = "info"
        private const val CARD_DETAILS_KEY = "cardDetails"
        private const val CARD_NETWORK_KEY = "cardNetwork"
        private const val BILLING_ADDRESS_KEY = "billingAddress"
        private const val TOKENIZATION_DATA_KEY = "tokenizationData"
        private const val TOKEN_KEY = "token"
        private const val LAST_FOUR_DIGIT_COUNT = 4
        private const val LAST_FOUR_REGEX = "[0-9]{${LAST_FOUR_DIGIT_COUNT},}"
    }
}