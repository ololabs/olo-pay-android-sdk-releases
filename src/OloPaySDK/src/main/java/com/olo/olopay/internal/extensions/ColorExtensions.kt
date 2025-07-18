// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.extensions

import android.graphics.Color
import androidx.annotation.RequiresApi
import com.olo.olopay.internal.data.GlobalConstants

// This isn't technically an extension function, but only because the Color class doesn't have a
// Companion object and so I couldn't create a static extension function. I'm leaving it here in
// hopes that we may be able to make it a true extension function in the future.
// See https://youtrack.jetbrains.com/issue/KT-11968
@RequiresApi(GlobalConstants.ApiOreo)
internal fun parseColorOrNull(color: String?): Int? {
    return color?.let {
        try {
            Color.parseColor(it)
        } catch (e: java.lang.Exception) {
            null
        }
    }
}