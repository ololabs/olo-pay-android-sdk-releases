// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import kotlinx.coroutines.flow.MutableStateFlow

interface IWorkerStatus {
    val isBusy: MutableStateFlow<Boolean>
}