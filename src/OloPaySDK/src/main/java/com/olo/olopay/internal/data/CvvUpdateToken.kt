// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import android.os.Parcelable
import com.olo.olopay.data.ICvvUpdateToken
import com.olo.olopay.data.OloPayEnvironment
import com.stripe.android.model.Token as StripeToken
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CvvUpdateToken constructor (internal val _cvvToken: StripeToken) :
    Parcelable, ICvvUpdateToken {

    override val id: String
        get() = _cvvToken.id

    override val environment: OloPayEnvironment
        get() = if (_cvvToken.livemode) OloPayEnvironment.Production else OloPayEnvironment.Test

    override fun toString(): String {
        val properties = listOf<String>(
            "${CvvUpdateToken::id.name}=${id}",
            "${CvvUpdateToken::environment.name}=${environment}",
        )

        return "${this.javaClass.name}(${properties.joinToString(", ")})"
    }
}