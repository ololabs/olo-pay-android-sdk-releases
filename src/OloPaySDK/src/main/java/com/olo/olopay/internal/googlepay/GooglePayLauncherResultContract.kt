// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.googlepay

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.olo.olopay.exceptions.GooglePayException
import com.olo.olopay.googlepay.GooglePayErrorType
import com.olo.olopay.googlepay.Result as GooglePayResult

internal class GooglePayLauncherResultContract : ActivityResultContract<GooglePayLauncherArgs, GooglePayResult>() {
    override fun createIntent(context: Context, input: GooglePayLauncherArgs): Intent {
        return Intent(context, GooglePayLauncherActivity::class.java)
            .putExtras(input.toBundle())
    }

    override fun parseResult(resultCode: Int, intent: Intent?): GooglePayResult {
        return intent?.getParcelableExtra(EXTRA_RESULT) ?:
            GooglePayResult.Failed(
                GooglePayException(IllegalArgumentException("Unable to parse a Google Pay result"),
                GooglePayErrorType.InternalError)
            )
    }

    internal companion object {
        const val EXTRA_RESULT = "extra_result"
    }
}