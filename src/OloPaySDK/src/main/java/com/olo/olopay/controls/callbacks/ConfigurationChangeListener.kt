// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.controls.callbacks

import android.content.res.Configuration

/**
 * A listener for configuration changes
 */
fun interface ConfigurationChangeListener {
    /**
     * Called whenever the device configuration changes
     * @param newConfig The new device configuration
     */
    fun onConfigurationChanged(newConfig: Configuration?)
}