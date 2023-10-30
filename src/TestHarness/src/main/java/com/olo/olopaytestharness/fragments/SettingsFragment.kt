// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import com.olo.olopaytestharness.R
import com.olo.olopaytestharness.databinding.FragmentSettingsBinding
import com.olo.olopaytestharness.viewmodels.SettingsViewModel

class SettingsFragment : DialogFragment() {
    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentSettingsBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        val toolbar = binding.toolbarLayout.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Settings"
        toolbar.inflateMenu(R.menu.settings_menu)
        toolbar.setOnMenuItemClickListener { _ ->
            //Assume done because that's the only menu item for now
            dismissAllowingStateLoss()
            true
        }

        return binding.root
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

        viewModel.notifySettingsChanged()
    }
}