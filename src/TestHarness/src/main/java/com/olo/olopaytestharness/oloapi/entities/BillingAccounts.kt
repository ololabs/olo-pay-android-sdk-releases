// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class BillingAccounts(
    @SerialName("billingaccounts")
    val billingAccounts: List<BillingAccount>?
) {
    override fun toString(): String {
        val properties = listOf(
            "${BillingAccounts::billingAccounts} = ${billingAccounts.toString()}"
        )
        return "${this.javaClass.name}(${properties.joinToString(", ")})"
    }
}