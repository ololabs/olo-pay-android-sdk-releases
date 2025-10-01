// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi.entities

import com.olo.olopay.data.ICvvUpdateToken
import com.olo.olopay.data.IPaymentMethod

class PaymentType private constructor(val paymentMethod: IPaymentMethod?, val cvvToken: ICvvUpdateToken?) {
    constructor(paymentMethod: IPaymentMethod): this(paymentMethod, null) {}
    constructor(token: ICvvUpdateToken): this(null, token){}
}