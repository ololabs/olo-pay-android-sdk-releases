// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

/**
 * Callback to know when Google Pay is ready
 */
fun interface GooglePayReadyCallback {
    /**
     * Called when Google Pay is ready. This can be used to enable/disable the Google Pay button
     * @param isReady `true` if Google Pay is ready, otherwise `false`
     */
    fun onReady(isReady: Boolean)
}