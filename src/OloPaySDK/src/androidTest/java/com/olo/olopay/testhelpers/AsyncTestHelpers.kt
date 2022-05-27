// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.testhelpers

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun waitForCondition(condition: BooleanWrapper, delayIntervalMs: Long = 500, maxDelayMs: Long = 2000) = runBlocking {
    var totalDelay = 0L
    while (!condition.value && totalDelay < maxDelayMs) {
        delay(delayIntervalMs)
        totalDelay += delayIntervalMs
    }
}