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
import com.olo.olopay.googlepay.GooglePayContext
import com.olo.olopay.googlepay.ReadyCallback
import com.olo.olopay.googlepay.ResultCallback
import com.olo.olopaytestharness.databinding.FragmentGooglePayBinding
import com.olo.olopaytestharness.viewmodels.GooglePayViewModel

class GooglePayFragment : Fragment() {
    private val args: GooglePayFragmentArgs by navArgs()

    private val viewModel: GooglePayViewModel by viewModels {
        if (args.useKotlin) GooglePayViewModel.KotlinFactory else GooglePayViewModel.JavaFactory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentGooglePayBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val googlePayContext = GooglePayContext(
            this,
            readyCallback = ReadyCallback(viewModel::onGooglePayReady),
            resultCallback = ResultCallback(viewModel::onGooglePayResult)
        )

        binding.googlePayContext = googlePayContext
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