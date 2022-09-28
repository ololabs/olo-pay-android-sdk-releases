// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

import com.stripe.android.googlepaylauncher.GooglePayEnvironment

/**
 * Enum for determining what environment Google Pay will use
 */
enum class Environment {
    /** The Production Google Pay environment */
    Production,

    /** The Test Google Pay environment */
    Test;

    /** @suppress */
    companion object {
        internal fun convertFrom(env: Environment): GooglePayEnvironment {
            return when(env) {
                Test -> GooglePayEnvironment.Test
                Production -> GooglePayEnvironment.Production
            }
        }
    }
}