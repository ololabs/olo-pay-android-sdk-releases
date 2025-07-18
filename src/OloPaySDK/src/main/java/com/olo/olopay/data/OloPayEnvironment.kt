// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

/**
 * Enum indicating the environment that should be used by the Olo Pay SDK
 * @property description A string representation of the enum
 */
public enum class OloPayEnvironment(val description: String) {
    /** Production environment **/
    Production("production"),
    /** Test environment **/
    Test("test")
}