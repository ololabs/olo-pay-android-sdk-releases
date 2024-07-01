// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

/** @suppress */
enum class SdkBuildType(val description: String) {
    /** @suppress */
    Internal("internal"),
    /** @suppress */
    Public("public");

    companion object {
        internal fun convertFrom(value: String): SdkBuildType? {
            return when (value.lowercase()) {
                Internal.description -> Internal
                Public.description -> Public
                else -> null
            }
        }
    }
}