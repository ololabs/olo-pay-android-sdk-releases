// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.models

import com.olo.olopaytestharness.models.callbacks.ISettingsChangedListener

abstract class Settings<T> {
    private val listeners: MutableList<ISettingsChangedListener<T>> = mutableListOf()

    fun addListener(listener: ISettingsChangedListener<T>) {
        listeners.add(listener)
    }

    fun removeListener(listener: ISettingsChangedListener<T>) {
        listeners.remove(listener)
    }

    protected fun notifySettingsChanged(settings: T) {
        listeners.forEach {
            it.onSettingsChanged(settings)
        }
    }

    abstract fun notifySettingsChanged()
}