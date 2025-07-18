// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

internal enum class PaymentMethodSource(val description: String) {
    SingleLineInput("singleLineInput"),
    MultiLineInput("multiLineInput"),
    FormInput("formInput"),
    GooglePay("googlePay")
}