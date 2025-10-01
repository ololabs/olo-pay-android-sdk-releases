// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.testhelpers

data class BooleanWrapper (var value: Boolean) {
    override fun toString(): String {
        return value.toString()
    }
}