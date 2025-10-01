// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

/**
 * Enum representing supported currency codes
 */
public enum class CurrencyCode(val code: String) {
    /** US Dollar **/
    USD("USD"),

    /** Canadian Dollar **/
    CAD("CAD");

    companion object {
        /**
         * Convenience method to convert a string to a CurrencyCode value
         * Returns null if the given code is not supported
         */
        public fun from(code: String): CurrencyCode? {
            return when (code.uppercase()) {
                CAD.code -> CAD
                USD.code -> USD
                else -> null
            }
        }
    }
}