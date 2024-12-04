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
import com.olo.olopaytestharness.databinding.FragmentCvvTokenBinding
import com.olo.olopaytestharness.viewmodels.CvvTokenViewModel

class CvvTokenFragment : Fragment() {
    private val args: CvvTokenFragmentArgs by navArgs()

    private val viewModel: CvvTokenViewModel by viewModels {
        if (args.useKotlin) CvvTokenViewModel.KotlinFactory else CvvTokenViewModel.JavaFactory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentCvvTokenBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.cvvView.cvvInputListener = viewModel

        // Setting this variable should not be necessary, but it is... see the layout file for details
        binding.cvvInput = binding.cvvView

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