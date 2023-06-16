// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.*
import android.text.method.MovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

// Helper class inspired from this SO post: https://stackoverflow.com/a/43290961
class AutoScrollTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    override fun getDefaultEditable(): Boolean { return false }

    override fun getDefaultMovementMethod(): MovementMethod {
        return CursorScrollingMovementMethod()
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        scrollToEnd()
    }

    fun scrollToEnd() {
        val editable = text as SpannableString
        Selection.setSelection(editable, editable.length)
    }

    private class CursorScrollingMovementMethod: ScrollingMovementMethod() {
        override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
            widget?.moveCursorToVisibleOffset()
            return super.onTouchEvent(widget, buffer, event)
        }
    }
}