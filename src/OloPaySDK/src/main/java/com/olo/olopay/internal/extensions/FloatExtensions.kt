// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.extensions

import android.content.Context

internal fun Float.dpToPx(context: Context): Float =
    (this * context.resources.displayMetrics.density)