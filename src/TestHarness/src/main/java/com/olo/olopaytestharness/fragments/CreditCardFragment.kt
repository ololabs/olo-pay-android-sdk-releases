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
import com.olo.olopaytestharness.databinding.FragmentCreditCardBinding
import com.olo.olopaytestharness.viewmodels.CreditCardViewModel

class CreditCardFragment : Fragment() {
    private val args: CreditCardFragmentArgs by navArgs()

    private val viewModel: CreditCardViewModel by viewModels {
        if (args.useKotlin) CreditCardViewModel.KotlinFactory else CreditCardViewModel.JavaFactory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentCreditCardBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.cardSingleLineView.cardInputListener = viewModel
        binding.cardMultiLineView.cardInputListener = viewModel
        binding.cardDetailsForm.formValidCallback = viewModel

        // Setting this variable should not be necessary, but it is... see the layout file for details
        binding.singleLineInput = binding.cardSingleLineView
        binding.multiLineInput = binding.cardMultiLineView
        binding.formInput = binding.cardDetailsForm

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