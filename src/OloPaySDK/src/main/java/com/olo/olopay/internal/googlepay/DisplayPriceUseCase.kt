// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.googlepay

import com.olo.olopay.data.CurrencyCode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.pow

internal class DisplayPriceUseCase(currencyCode: CurrencyCode) {

    private val currency = Currency.getInstance(currencyCode.code)

    private fun getDisplayFormat(price: Int): String {
        val decimalPlaces = currency.defaultFractionDigits
        val totalLength = price.absoluteValue.toString().length

        val builder = StringBuilder()

        val wholeNumberPlaces = totalLength - decimalPlaces
        for (i in 0 until wholeNumberPlaces) {
            builder.append('#')
        }

        if (totalLength <= decimalPlaces) {
            builder.append('0')
        }

        builder.append('.')
        for (i in 0 until decimalPlaces) {
            builder.append('0')
        }

        return builder.toString()
    }

    operator fun invoke(price: Int): String {
        val decimalFormat = DecimalFormat(getDisplayFormat(price), DecimalFormatSymbols.getInstance(
            Locale.ROOT))
        decimalFormat.currency = currency
        decimalFormat.isGroupingUsed = false

        val decimalPrice = price / (10.0.pow(currency.defaultFractionDigits.toDouble()))
        return decimalFormat.format(decimalPrice)
    }
}