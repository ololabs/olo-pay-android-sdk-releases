// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class BillingAccount(
    @SerialName("accountid")
    val accountId: Long,

    @SerialName("accountidstring")
    val accountIdString: String
) {
    override fun toString(): String {
        val properties = listOf(
            "${BillingAccount::accountId} = $accountId",
            "${BillingAccount::accountIdString} = $accountIdString",
        )
        return "${this.javaClass.name}(${properties.joinToString(", ")}"
    }
}