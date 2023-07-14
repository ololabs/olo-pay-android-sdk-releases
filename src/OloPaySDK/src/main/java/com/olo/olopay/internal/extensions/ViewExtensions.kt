// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

internal fun View.requestFocus(requestShowKeyboard: Boolean) {
    post {
        if (this.requestFocus() && requestShowKeyboard) {
            val inputMethodMgr = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodMgr?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

internal fun View.dismissKeyboard() {
    val inputMethodMgr = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    inputMethodMgr?.hideSoftInputFromWindow(windowToken, 0)
}