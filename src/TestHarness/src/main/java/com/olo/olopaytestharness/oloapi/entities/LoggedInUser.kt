// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi.entities

import kotlinx.serialization.Serializable

@Serializable
class LoggedInUser(
    val token: String,
    val user: User,
) {
    override fun toString(): String {
        val properties = listOf(
            "${LoggedInUser::token.name} = $token",
            "${LoggedInUser::user.name} = $user"
        )
        return "${this.javaClass.name}(${properties.joinToString(", ")}"
    }
}
