// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import android.text.Editable
import android.text.TextWatcher

internal open class OloTextWatcher : TextWatcher {
    override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(editable: Editable?) {}
}