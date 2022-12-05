// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

import com.olo.olopay.googlepay.Config

/**
 * Parameters for setting up the Olo Pay API
 * @property freshSetup If `true`, this will be treated as a fresh setup of the API and cached values will
 *                   be overwritten. This is especially useful for testing purposes when switching
 *                   between Dev and Production environments. __This should generally be false for production builds__
 * @property googlePayConfig Configuration parameters for Google Pay
 *
 * @constructor Creates a new [SetupParameters] instance
 * @param freshSetup If `true`, this will be treated as a fresh setup of the API and cached values will
 *                   be overwritten. This is especially useful for testing purposes when switching
 *                   between Dev and Production environments. __This should generally be false for production builds__
 * @param googlePayConfig Configuration parameters for Google Pay
 * @param environment The environment the SDK will run in
 */
data class SetupParameters @JvmOverloads constructor (
    val environment: OloPayEnvironment = OloPayEnvironment.Production,
    val freshSetup: Boolean = false,
    val googlePayConfig: Config? = null
)