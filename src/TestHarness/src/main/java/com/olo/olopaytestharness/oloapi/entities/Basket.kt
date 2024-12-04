// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Basket(
    val id: String,

    @SerialName("vendorid")
    var vendorId: Int? = null,

    var mode: String? = null,

    @SerialName("deliverymode")
    var deliveryMode: String? = null,

    @SerialName("timewanted")
    var timeWanted: String? = null,

    val total: Double,

    var products: MutableList<Product?>? = null
) {
    override fun toString(): String {
        val properties = listOf(
            "${Basket::id.name}=${id}",
            "${Basket::vendorId.name}=${vendorId}",
            "${Basket::mode.name}=${mode}",
            "${Basket::deliveryMode.name}=${deliveryMode}",
            "${Basket::timeWanted.name}=${timeWanted}",
            "${Basket::total.name}=${total}",
            "${Basket::products.name}=${arrayOf(products).joinToString(", ")}"
        )

        return "${this.javaClass.name}(${properties.joinToString(", ")})"
    }
}