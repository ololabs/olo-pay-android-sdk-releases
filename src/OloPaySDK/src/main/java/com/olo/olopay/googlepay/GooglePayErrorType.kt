// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

import com.stripe.android.googlepaylauncher.GooglePayPaymentMethodLauncher

/**
 * An enum representing types of Google Pay Errors
 */
enum class GooglePayErrorType {
    /** Generic internal error */
    InternalError,

    /** The application is misconfigured */
    DeveloperError,

    /** Error executing a network call */
    NetworkError;

    /** @suppress */
    companion object {
        internal fun from(@GooglePayPaymentMethodLauncher.ErrorCode errorCode: Int) : GooglePayErrorType {
            return when (errorCode) {
                GooglePayPaymentMethodLauncher.DEVELOPER_ERROR -> DeveloperError
                GooglePayPaymentMethodLauncher.NETWORK_ERROR -> NetworkError
                else -> InternalError
            }
        }
    }
}