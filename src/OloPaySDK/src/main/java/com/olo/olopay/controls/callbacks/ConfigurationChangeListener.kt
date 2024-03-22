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