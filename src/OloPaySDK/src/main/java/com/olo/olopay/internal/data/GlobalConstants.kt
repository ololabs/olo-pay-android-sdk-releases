// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import android.os.Build

internal class GlobalConstants {
    companion object {
        const val ApiOreo = Build.VERSION_CODES.O_MR1
        const val ApiQuinceTart = Build.VERSION_CODES.Q
    }
}