// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

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

internal fun View.setBackgroundStyle(backgroundColor: Int? = null, borderColor: Int? = null,
                                            borderWidthPx: Float? = null, borderRadiusPx: Float? = null) {
    val borderRadius = if(borderRadiusPx == null || borderRadiusPx < 0.0F) 0.0F else borderRadiusPx
    val borderWidth = if(borderWidthPx == null || borderWidthPx < 0.0F) 0.0F else borderWidthPx

    this.background = MaterialShapeDrawable(
        ShapeAppearanceModel()
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, borderRadius)
            .build()
    ).also { shape ->
        shape.strokeWidth = borderWidth

        shape.strokeColor = ColorStateList.valueOf(borderColor ?: Color.TRANSPARENT)

        shape.fillColor = ColorStateList.valueOf(backgroundColor ?: Color.TRANSPARENT)
    }
}

internal fun View.isDescendantOf(targetView: View): Boolean {
    return findAncestor(this, targetView)
}

private fun findAncestor(child: View, ancestor: View): Boolean {
    val viewParent = child.parent as? View
    if (viewParent != null) {
        if (viewParent == ancestor) {
            return true
        }

        return findAncestor(viewParent, ancestor)
    }

    return false
}