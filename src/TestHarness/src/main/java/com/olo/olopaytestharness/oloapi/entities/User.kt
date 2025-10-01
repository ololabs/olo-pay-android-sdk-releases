// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("authtoken")
    val authToken: String?,

    @SerialName("emailaddress")
    val emailAddress: String,

    @SerialName("firstname")
    val firstName: String,

    @SerialName("lastname")
    val lastName: String
) {
    override fun toString(): String {
        val properties = listOf(
            "${User::authToken.name} = $authToken",
            "${User::emailAddress.name} = $emailAddress",
            "${User::firstName.name} = $firstName",
            "${User::lastName.name} = $lastName",
        )
        return "${this.javaClass.name}(${properties.joinToString(", ")}"
    }
}

