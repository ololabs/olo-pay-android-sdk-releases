// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.googlepay

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.olo.olopay.exceptions.GooglePayException
import com.olo.olopay.googlepay.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class GooglePayLauncher private constructor(
    context: Context,
    lifecycleScope: CoroutineScope,
    config: Config
) {
    private var _isReady = false
    private var _readyCallback: ReadyCallback? = null
    private val _config: Config
    private val _googlePayRepository: GooglePayRepository
    private lateinit var _activityResultLauncher: ActivityResultLauncher<GooglePayLauncherArgs>

    internal constructor(activity: ComponentActivity, config: Config): this(
        activity as Context,
        activity.lifecycleScope,
        config
    ) {
        _activityResultLauncher = activity.registerForActivityResult(GooglePayLauncherResultContract()) {
            resultCallback?.onResult(it)
        }
    }

    internal constructor(fragment: Fragment, config: Config): this(
        fragment.requireContext(),
        fragment.viewLifecycleOwner.lifecycleScope,
        config
    ) {
        _activityResultLauncher = fragment.registerForActivityResult(GooglePayLauncherResultContract()) {
            resultCallback?.onResult(it)
        }
    }

    init {
        try {
            _config = config
            _googlePayRepository = GooglePayRepository(context, _config)
        } catch (e: Exception) {
            throw GooglePayException(e, GooglePayErrorType.DeveloperError)
        }

        lifecycleScope.launch {
            onGooglePayReady(_googlePayRepository.isReady().first())
        }
    }

    internal var readyCallback
        get() = _readyCallback
        set(value) {
            _readyCallback = value
            _readyCallback?.onReady(_isReady)
        }

    internal var resultCallback: ResultCallback? = null

    @JvmOverloads
    internal fun present(currencyCode: String, amount: Int = 0, transactionId: String? = null) {
        check(_isReady) {
            "present() may only be called when Google Pay is available on this device"
        }

        _activityResultLauncher.launch(
            GooglePayLauncherArgs(_config, currencyCode, amount, transactionId)
        )
    }

    private fun onGooglePayReady(isReady: Boolean) {
        _isReady = isReady
        readyCallback?.onReady(isReady)
    }
}