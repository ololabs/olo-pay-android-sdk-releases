// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.api

/**
 * Interface for returning results from API calls
 * <hr class="spacer">
 *
 * #### Important:
 * _Under normal circumstances this would only be used if writing code in Java, as every API
 * call supports coroutines_
 */
interface ApiResultCallback<in T> {
    /**
     * Called when an API call is successful
     * @param result The result of the API call
     */
    fun onSuccess(result: T)

    /**
     * Called when an API call fails
     * @param e The exception that occurred
     */
    fun onError(e: Exception)
}