// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.googlepay

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.olo.olopay.exceptions.GooglePayException
import com.olo.olopay.googlepay.GooglePayErrorType
import com.olo.olopay.googlepay.Result as GooglePayResult
import kotlinx.coroutines.launch

internal class GooglePayLauncherActivity: AppCompatActivity() {
    private lateinit var _launcherArgs: GooglePayLauncherArgs

    private val _viewModel by viewModels<GooglePayLauncherViewModel> {
        GooglePayLauncherViewModel.Factory(
            _launcherArgs,
            GooglePayRepository(this.applicationContext, _launcherArgs.config)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launcherArgs = GooglePayLauncherArgs.fromIntent(intent)
        if (launcherArgs == null) {
            finish(
                GooglePayResult.Failed(GooglePayException(
                    RuntimeException("GooglePayLauncherActivity was started without arguments"),
                    GooglePayErrorType.DeveloperError))
            )
            return
        }

        _launcherArgs = launcherArgs

        _viewModel.googlePayResult.observe(this) { googlePayResult ->
            googlePayResult?.let(::finish)
        }

        if (_viewModel.hasLaunched)
            return

        lifecycleScope.launch {
            runCatching {
                _viewModel.createPaymentDataTask()
            }.fold(
                onSuccess = { task ->
                    launchGooglePay(task)
                },
                onFailure = { error ->
                    _viewModel.updateResult(
                        GooglePayResult.Failed(GooglePayException(error, GooglePayErrorType.InternalError))
                    )
                }
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != LOAD_PAYMENT_DATA_REQUEST_CODE)
            return

        when (resultCode) {
            RESULT_OK -> onGooglePayResult(data)
            RESULT_CANCELED -> _viewModel.updateResult(GooglePayResult.Canceled)
            AutoResolveHelper.RESULT_ERROR -> onGooglePayError(data)
            else -> {
                _viewModel.updateResult(GooglePayResult.Failed(GooglePayException(
                    RuntimeException("Google Pay returned an unexpected result code"),
                    GooglePayErrorType.InternalError))
                )
            }
        }
    }

    private fun onGooglePayResult(data: Intent?) {
        data?.let { PaymentData.getFromIntent(it) }?.let { paymentData ->
            lifecycleScope.launch {
                finish(_viewModel.createPaymentMethod(this@GooglePayLauncherActivity.applicationContext, paymentData))
            }
        } ?: _viewModel.updateResult(GooglePayResult.Failed(GooglePayException(
                IllegalArgumentException("Google Pay Data was not available"),
                GooglePayErrorType.InternalError))
            )
    }

    private fun onGooglePayError(data: Intent?) {
        val status = AutoResolveHelper.getStatusFromIntent(data)
        val statusMessage = status?.statusMessage.orEmpty()
        val errorType = status?.statusCode?.let { googlePayStatusCodeToErrorType(it) } ?: GooglePayErrorType.InternalError

        _viewModel.updateResult(GooglePayResult.Failed(GooglePayException(
            RuntimeException("Google Pay failed with error ${status?.statusCode}: $statusMessage"),
            errorType))
        )
    }

    private fun googlePayStatusCodeToErrorType(googlePayStatusCode: Int): GooglePayErrorType {
        return when (googlePayStatusCode) {
            CommonStatusCodes.NETWORK_ERROR -> GooglePayErrorType.NetworkError
            CommonStatusCodes.DEVELOPER_ERROR -> GooglePayErrorType.DeveloperError
            else -> GooglePayErrorType.InternalError
        }
    }

    private fun launchGooglePay(task: Task<PaymentData>) {
        AutoResolveHelper.resolveTask(task, this, LOAD_PAYMENT_DATA_REQUEST_CODE)
        _viewModel.hasLaunched = true
    }

    private fun finish(result: GooglePayResult) {
        setResult(RESULT_OK, Intent().putExtras(bundleOf(GooglePayLauncherResultContract.EXTRA_RESULT to result)))
        finish()
    }

    private companion object {
        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 5555
    }
}