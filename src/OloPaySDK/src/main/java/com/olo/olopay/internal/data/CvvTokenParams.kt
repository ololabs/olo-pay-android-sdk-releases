// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import com.olo.olopay.data.ICvvTokenParams

internal class CvvTokenParams internal constructor(
    private var _cvvValue: String
): ICvvTokenParams {
    internal val cvvValue: String
        get() = _cvvValue
}