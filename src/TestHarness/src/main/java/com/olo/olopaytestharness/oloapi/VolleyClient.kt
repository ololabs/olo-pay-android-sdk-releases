// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.oloapi

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.olo.olopay.bootstrap.ApplicationProvider

class VolleyClient constructor(context: Context) {
    companion object {
        @Volatile
        private var _instance: VolleyClient? = null

        val instance: VolleyClient
            get() {
                return _instance ?: synchronized(this) {
                    VolleyClient(ApplicationProvider.currentApplication!!).also {
                        _instance = it
                    }
                }
            }
    }

    fun<T> addToQueue(request: Request<T>) {
        requestQueue.add(request)
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(ApplicationProvider.currentApplication!!)
    }
}