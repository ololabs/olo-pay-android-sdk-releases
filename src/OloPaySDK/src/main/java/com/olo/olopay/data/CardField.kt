// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data
import android.content.Context
import com.olo.olopay.R
import com.stripe.android.view.CardValidCallback.Fields as StripeCardField
import com.stripe.android.view.CardInputListener.FocusField as StripeFocusField

/**
 * An enum representing the fields of a credit/debit card
 */
enum class CardField(
    private val emptyError: Int,
    private val invalidError: Int,
    private val unsupportedError: Int? = null) {
    /** The card number field */
    CardNumber(
        R.string.olopay_empty_card_number_error,
        R.string.olopay_invalid_card_number_error,
        R.string.olopay_unsupported_card_type_error),

    /** The expiration field */
    Expiration(
        R.string.olopay_empty_expiration_error,
        R.string.olopay_invalid_expiration_error),

    /** The CVV security code field */
    Cvv(
        R.string.olopay_empty_cvv_error,
        R.string.olopay_invalid_cvv_error),

    /** The zip/postal code field */
    PostalCode(
        R.string.olopay_empty_postal_code_error,
        R.string.olopay_invalid_postal_code_error);

    internal fun getError(context: Context, isEmpty: Boolean, isUnsupported: Boolean = false): String {
        return if (isEmpty) {
            context.getString(emptyError)
        } else if (isUnsupported && unsupportedError != null) {
            context.getString(unsupportedError)
        } else {
            context.getString(invalidError)
        }
    }

    /** @suppress */
    companion object {
        internal fun from(field: StripeCardField): CardField {
            return when (field) {
                StripeCardField.Number -> CardNumber
                StripeCardField.Expiry -> Expiration
                StripeCardField.Cvc -> Cvv
                StripeCardField.Postal -> PostalCode
            }
        }

        internal fun from(field: StripeFocusField): CardField {
            return when (field) {
                StripeFocusField.CardNumber -> CardNumber
                StripeFocusField.ExpiryDate -> Expiration
                StripeFocusField.Cvc -> Cvv
                StripeFocusField.PostalCode -> PostalCode
            }
        }
    }
}