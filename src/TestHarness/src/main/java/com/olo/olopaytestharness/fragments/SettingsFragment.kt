// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.fragments

import android.content.DialogInterface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.olo.olopaytestharness.databinding.SettingsFragmentBinding
import androidx.appcompat.widget.Toolbar
import com.olo.olopaytestharness.R
import com.olo.olopaytestharness.viewmodels.SettingsViewModel


class SettingsFragment : DialogFragment() {
    private lateinit var binding: SettingsFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SettingsFragmentBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = ViewModelProvider(requireActivity()).get(SettingsViewModel::class.java)

        val toolbar = binding.toolbarLayout.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Settings"
        toolbar.inflateMenu(R.menu.settings_menu)
        toolbar.setOnMenuItemClickListener { _ ->
            //Assume done because that's the only menu item for now
            dismissAllowingStateLoss()
            true
        }
    }

    override fun onResume() {
        super.onResume()

        // Hack to get dialog fragment to respect match_parent width and height
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val dialogActivity = activity
        if (dialogActivity is DialogInterface.OnDismissListener) {
            dialogActivity.onDismiss(dialog)
        }
    }
}