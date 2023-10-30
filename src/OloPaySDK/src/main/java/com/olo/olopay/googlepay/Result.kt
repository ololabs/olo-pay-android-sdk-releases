// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

import android.os.Parcelable
import com.olo.olopay.data.IPaymentMethod
import kotlinx.parcelize.Parcelize
import com.olo.olopay.exceptions.GooglePayException

/**
 * Result class for generating payment methods via Google Pay
 */
sealed class Result : Parcelable {
    /**
     * Represents a successful transaction
     * @param paymentMethod The resulting payment method
     * @property paymentMethod The resulting payment method
     */
    @Parcelize
    data class Completed(val paymentMethod: IPaymentMethod) : Result()

    /**
     * Represents a failed transaction
     * @param error: The exception representing the error
     * @property error: The exception representing the error
     */
    @Parcelize
    data class Failed(val error: GooglePayException) : Result()

    /**
     * Represents a transaction canceled by the user
     */
    @Parcelize
    object Canceled : Result()
}