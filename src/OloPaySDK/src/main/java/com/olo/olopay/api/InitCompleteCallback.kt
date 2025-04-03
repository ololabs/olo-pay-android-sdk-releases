// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.api

/**
 * Interface for executing code immediately after the SDK has been initialized.
 * <hr class="spacer">
 *
 * #### Important:
 * Under normal circumstances this would only be used if writing code in Java
 */
interface InitCompleteCallback {
    /** Called when the SDK has been initialized **/
    fun onComplete()
}