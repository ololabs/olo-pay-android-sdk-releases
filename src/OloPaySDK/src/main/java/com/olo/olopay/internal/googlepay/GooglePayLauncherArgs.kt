// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.googlepay

import android.content.Intent
import android.os.Parcelable
import androidx.core.os.bundleOf
import com.olo.olopay.googlepay.GooglePayCheckoutStatus
import com.olo.olopay.googlepay.GooglePayConfig
import com.olo.olopay.googlepay.GooglePayLineItem
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class GooglePayLauncherArgs internal constructor(
    internal val config: GooglePayConfig,
    internal val amount: Int,
    internal val transactionId: String? = null,
    internal val checkoutStatus: GooglePayCheckoutStatus,
    internal val totalPriceLabel: String,
    internal val lineItems: List<GooglePayLineItem>?
) : Parcelable {

    internal fun toBundle() = bundleOf(EXTRA_ARGS to this)

    internal companion object {
        private const val EXTRA_ARGS = "extra_args"

        internal fun fromIntent(intent: Intent): GooglePayLauncherArgs? {
            return intent.getParcelableExtra(EXTRA_ARGS)
        }
    }
}