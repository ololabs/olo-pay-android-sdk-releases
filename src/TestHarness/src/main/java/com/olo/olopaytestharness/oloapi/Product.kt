// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: ULong,
    val productId: ULong,
    val name: String,
    val quantity: Int
) {
    override fun toString(): String {
        val properties = listOf(
            "${Product::id.name}=${id}",
            "${Product::productId.name}=${productId}",
            "${Product::name.name}=${name}",
            "${Product::quantity.name}=${quantity}",
        )

        return "${this.javaClass.name}(${properties.joinToString(", ")})"
    }
}