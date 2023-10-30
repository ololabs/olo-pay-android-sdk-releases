// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

/**
 * Interface representing the current state of a CardField. This is useful if you want to provide
 * a custom error messages UI
 */
interface ICardFieldState {
    /**
     * Whether or not the field is in a valid state. There are some special considerations for
     * postal code fields
     *
     * - If the postal code field is not enabled or not required it will always be valid
     * - If the postal code is required it is considered valid if the field has ANY text
     * - Additional postal code validation is handled server side when creating a payment method
     *   and/or submitting a basket for payment
     */
    val isValid: Boolean

    /**
     * Whether or not the field currently has focus. Only one field can have focus at a time.
     */
    val isFocused: Boolean

    /**
     * Whether or not the field has any text
     */
    val isEmpty: Boolean

    /**
     * Whether or not the user has entered text in the field.
     * This can be true even if the field is empty (e.g. if the user entered text and then deleted it)
     */
    val wasEdited: Boolean

    /**
     * Whether or not the field had ever received focus
     */
    val wasFocused: Boolean
}