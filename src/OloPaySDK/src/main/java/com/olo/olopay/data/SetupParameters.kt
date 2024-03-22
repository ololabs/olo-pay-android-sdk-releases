// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

import com.olo.olopay.googlepay.Config

/**
 * Parameters for setting up the Olo Pay API
 * @param environment The environment the SDK will run in
 * @property freshSetup This property is deprecated and will be removed in a future release
 * @property googlePayConfig Configuration parameters for Google Pay
 *
 * @constructor This constructor is deprecated. Use alternative constructors without the [freshSetup] parameter
 */
data class SetupParameters @Deprecated(
    "Use alternative constructors without freshSetup parameter",
    level = DeprecationLevel.WARNING
)
constructor(
    val environment: OloPayEnvironment,
    val freshSetup: Boolean,
    val googlePayConfig: Config?
) {
    /**
     * Creates a new [SetupParameters] instance
     * @param googlePayConfig Configuration parameters for Google Pay
     * @param environment The environment the SDK will run in
     */
    @JvmOverloads constructor(
        environment: OloPayEnvironment = OloPayEnvironment.Production,
        googlePayConfig: Config? = null
    ) : this(environment = environment, googlePayConfig = googlePayConfig, freshSetup = false)
}
