// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models.callbacks

fun interface ISettingsChangedListener<T> {
    fun onSettingsChanged(settings: T)
}