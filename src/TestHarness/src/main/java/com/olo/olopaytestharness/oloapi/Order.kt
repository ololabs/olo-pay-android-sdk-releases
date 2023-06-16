// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi

import kotlinx.serialization.Serializable

@Serializable
data class Order(val id: String) {
    override fun toString() : String {
        return "${this.javaClass.name}(id=${id})"
    }
}