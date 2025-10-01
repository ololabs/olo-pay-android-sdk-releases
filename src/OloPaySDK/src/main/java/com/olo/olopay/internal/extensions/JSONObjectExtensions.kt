// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.extensions

import org.json.JSONObject

@Suppress("UNCHECKED_CAST")
internal fun <T> JSONObject.getOrDefault(key: String, defaultValue: T): T {
    return try {
        this.get(key) as T
    } catch (e: Exception) {
        defaultValue
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <T> JSONObject.getOrNull(key: String): T? {
    return try {
        this.get(key) as T
    } catch (e: Exception) {
        null
    }
}