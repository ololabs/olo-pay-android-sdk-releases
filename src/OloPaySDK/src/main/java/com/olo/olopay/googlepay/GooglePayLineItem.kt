// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

import android.os.Parcelable
import com.olo.olopay.data.CurrencyCode
import com.olo.olopay.internal.googlepay.DisplayPriceUseCase
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents a line item to be displayed in the Google Pay sheet.
 */
@Parcelize
data class GooglePayLineItem(
    /**
     * The label for the line item that will appear on the Google Payment sheet
     */
    val label: String,

    /**
     * The price for the line item that will appear on the Google Pay sheet.
     * It should be an integer representing the price in the smallest currency unit (e.g. 100 cents to represent $1.00)
     */
    val price: Int,

    /**
     * The type of line line. See [GooglePayLineItemType] for documentation
     */
    val type: GooglePayLineItemType,

    /**
     * The status of the line item. Default is `GooglePayLineItemStatus.Final`
     * See [GooglePayLineItemStatus] for documentation
     */
    val status: GooglePayLineItemStatus = GooglePayLineItemStatus.Final
): Parcelable {
    internal fun toJson(currencyCode: CurrencyCode): JSONObject {
        val displayPriceUseCase = DisplayPriceUseCase(currencyCode)

        return JSONObject()
            .put("label", label)
            .put("price", displayPriceUseCase(price))
            .put("type", type.value)
            .put("status", status.value)
    }
}
