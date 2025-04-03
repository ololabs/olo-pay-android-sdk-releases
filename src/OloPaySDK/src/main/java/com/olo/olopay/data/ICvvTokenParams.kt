// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

import com.olo.olopay.api.OloPayAPI
import com.olo.olopay.controls.PaymentCardCvvView

/**
 * Class used to create an [ICvvUpdateToken] instance from user input. Instances of this class can be retrieved
 * from [PaymentCardCvvView]
 * @see OloPayAPI.createCvvUpdateToken
 */
interface ICvvTokenParams {
}
