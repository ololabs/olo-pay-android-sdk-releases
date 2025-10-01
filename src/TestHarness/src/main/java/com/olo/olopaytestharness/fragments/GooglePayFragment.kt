// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.olo.olopay.googlepay.GooglePayConfig
import com.olo.olopay.googlepay.GooglePayEnvironment
import com.olo.olopay.googlepay.GooglePayLauncher
import com.olo.olopay.googlepay.GooglePayReadyCallback
import com.olo.olopay.googlepay.GooglePayResultCallback
import com.olo.olopaytestharness.R
import com.olo.olopaytestharness.databinding.FragmentGooglePayBinding
import com.olo.olopaytestharness.viewmodels.GooglePayViewModel

class GooglePayFragment : Fragment() {
    private val args: GooglePayFragmentArgs by navArgs()
    private lateinit var binding: FragmentGooglePayBinding

    private val viewModel: GooglePayViewModel by viewModels {
        if (args.useKotlin) GooglePayViewModel.KotlinFactory else GooglePayViewModel.JavaFactory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGooglePayBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val appContext = requireContext().applicationContext

        val googlePayEnv = if (appContext.resources.getBoolean(R.bool.google_pay_production_env)) {
            GooglePayEnvironment.Production
        } else {
            GooglePayEnvironment.Test
        }

        val existingPaymentMethodsRequired =
            appContext.resources.getBoolean(R.bool.google_pay_existing_payment_methods_required)

        val googlePayConfig = GooglePayConfig(
            environment = googlePayEnv,
            companyName = "Olo Pay SDK",
            existingPaymentMethodRequired = existingPaymentMethodsRequired
        )

        val googlePayLauncher = GooglePayLauncher(
            fragment = this,
            config = googlePayConfig,
            readyCallback = GooglePayReadyCallback(viewModel::onGooglePayReady),
            resultCallback = GooglePayResultCallback(viewModel::onGooglePayResult)
        )

        binding.googlePayLauncher = googlePayLauncher
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }
}