// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi

import com.android.volley.Request

enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE;

    fun toVolleyMethod(): Int {
        return when(this) {
            GET -> Request.Method.GET
            POST -> Request.Method.POST
            PUT -> Request.Method.POST
            DELETE -> Request.Method.POST
        }
    }
}