// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.exceptions

import com.olo.olopay.googlepay.GooglePayErrorType

/**
 * An exception having to do with Google Pay payments
 * @property errorType The type of error
 * @see GooglePayErrorType
 *
 * @constructor Create an instance of this class
 * @param throwable The throwable for this exception
 * @param errorType The type of Google Pay error this exception represents
 */
class GooglePayException constructor(throwable: Throwable, val errorType: GooglePayErrorType) : OloPayException(throwable) {
}