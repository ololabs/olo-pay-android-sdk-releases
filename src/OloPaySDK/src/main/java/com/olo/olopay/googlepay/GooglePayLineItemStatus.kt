// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.googlepay

/**
 * Represents the status of a line item
 */
enum class GooglePayLineItemStatus(val value: String) {
    /** Indicates that this price is final and has no variance */
    Final("FINAL"),

    /** Indicates that this price is pending and may change */
    Pending("PENDING")
}