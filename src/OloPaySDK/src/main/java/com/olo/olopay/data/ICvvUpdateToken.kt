// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

/**
 * Represents a CVV update token needed to revalidate previously used credit/debit cards on file.
 */
interface ICvvUpdateToken {
    /**
     * The CVV update token id.
     */
    val id: String

    /**
     * The Environment that the CVV update token can work in.
     */
    val environment: OloPayEnvironment
}