// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

/**
 * Callback for Google Pay results
 */
fun interface GooglePayResultCallback {
    /**
     * Called when the Google Pay flow has finished
     * @param result The result of the Google Pay flow
     */
    fun onResult(result: GooglePayResult)
}