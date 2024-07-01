// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.controls

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

internal class PaymentCardCvvEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    internal val cvvValue
        get() = this.text.toString()

}
