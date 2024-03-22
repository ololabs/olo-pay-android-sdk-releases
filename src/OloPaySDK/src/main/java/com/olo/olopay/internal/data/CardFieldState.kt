// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import com.olo.olopay.data.ICardFieldState

internal data class CardFieldState(
    private var _isValid: Boolean = false,
    private var _isFocused: Boolean = false,
    private var _isEmpty: Boolean = true,
    private var _wasEdited: Boolean = false,
    private var _wasFocused: Boolean = false)
    : ICardFieldState {

    override val isValid: Boolean
        get() = _isValid

    internal fun setValid(valid: Boolean) { _isValid = valid }

    override val isFocused
        get() = _isFocused

    internal fun setIsFocused(focused: Boolean) { _isFocused = focused}

    override val isEmpty: Boolean
        get() = _isEmpty

    internal fun setEmpty(empty: Boolean) { _isEmpty = empty }

    override val wasEdited: Boolean
        get() = _wasEdited

    internal fun setEdited(edited: Boolean) { _wasEdited = edited }

    override val wasFocused
        get() = _wasFocused

    internal fun setWasFocused(focused: Boolean) { _wasFocused = focused}

    internal fun reset() {
        _isValid = false
        _isFocused = false
        _isEmpty = true
        _wasEdited = false
        _wasFocused = false
    }

    override fun toString(): String {
        val properties = listOf(
            "${CardFieldState::isValid.name}=${isValid}",
            "${CardFieldState::isFocused.name}=${isFocused}",
            "${CardFieldState::isEmpty.name}=${isEmpty}",
            "${CardFieldState::wasEdited.name}=${wasEdited}",
            "${CardFieldState::wasFocused.name}=${wasFocused}"
        )

        return "${this.javaClass.name}(${properties.joinToString(", ")})"
    }
}