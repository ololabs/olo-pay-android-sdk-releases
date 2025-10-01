// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.converters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import java.text.NumberFormat
import java.util.Currency


object ViewGroupVisibilityAdapter {
    @BindingAdapter("android:visibility")
    @JvmStatic fun setVisibility(view: ViewGroup, visible: Boolean) {
        view.isVisible = visible
    }
}

object ViewEnabledAdapter {
    @BindingAdapter("android:enabled")
    @JvmStatic fun setEnabled(view: View, enabled: Boolean) {
        view.isEnabled = enabled
    }

    @InverseBindingAdapter(attribute = "android:enabled")
    @JvmStatic fun isEnabled(view: View): Boolean = view.isEnabled

    @BindingAdapter("android:enabledAttrChanged")
    @JvmStatic fun setListeners(view: View, attrChanged: InverseBindingListener) {}
}

object PriceAdapter {
    @BindingAdapter("currencyCode", "amount")
    @JvmStatic fun setCurrencyAndAmount(textView: TextView, currencyCode: String, amount: Int) {
        val format: NumberFormat = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 2
        format.currency = Currency.getInstance(currencyCode.uppercase())

        textView.text = format.format(amount / 100.0F)
    }
}