// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

/**
 * An enum representing types of Google Pay Errors
 */
enum class GooglePayErrorType {
    /** Generic internal error */
    InternalError,

    /** The application is misconfigured */
    DeveloperError,

    /** Error executing a network call */
    NetworkError,

    /** The Google Pay flow was initiated before it was ready **/
    NotReadyError,

    /** The company name is empty **/
    EmptyCompanyNameError,

    /** The company country code is empty **/
    EmptyCountryCodeError,

    /** The company country code is not 2 characters **/
    InvalidCountryCodeError,

    /** The line items sum total does not equal the total price **/
    LineItemTotalMismatchError,
}