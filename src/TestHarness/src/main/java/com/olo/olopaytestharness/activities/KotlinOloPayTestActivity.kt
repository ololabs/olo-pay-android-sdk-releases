// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.olo.olopay.controls.callbacks.CardInputListener
import com.olo.olopay.controls.callbacks.FormValidCallback
import com.olo.olopay.data.CardField
import com.olo.olopay.data.ICardFieldState
import com.olo.olopay.googlepay.GooglePayContext
import com.olo.olopay.googlepay.IGooglePayContext
import com.olo.olopay.googlepay.Result
import com.olo.olopaytestharness.R
import com.olo.olopaytestharness.fragments.SettingsFragment
import com.olo.olopaytestharness.databinding.ActivityOloPayTestBinding
import com.olo.olopaytestharness.olopaysdk.KotlinSDKImplementation
import com.olo.olopaytestharness.util.AppUtils
import com.olo.olopaytestharness.viewmodels.ActivityViewModel
import com.olo.olopaytestharness.viewmodels.SettingsViewModel

import kotlinx.serialization.ExperimentalSerializationApi


@ExperimentalSerializationApi
class KotlinOloPayTestActivity : AppCompatActivity(), CardInputListener, FormValidCallback, DialogInterface.OnDismissListener {
    private val settingsTag: String = "SettingsDialog"
    private lateinit var _binding: ActivityOloPayTestBinding
    private lateinit var _googlePayContext: IGooglePayContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_olo_pay_test)
        _binding.lifecycleOwner = this

        val provider = ViewModelProvider(this)
        _binding.settings = provider[SettingsViewModel::class.java]
        _binding.viewModel = provider[ActivityViewModel::class.java]
        _binding.sdkImpl = KotlinSDKImplementation(_binding.viewModel as ActivityViewModel, _binding.settings as SettingsViewModel)

        _googlePayContext = GooglePayContext(
            this,
            readyCallback = { isReady: Boolean -> (_binding.viewModel as ActivityViewModel).googlePayReady.postValue(isReady) },
            resultCallback = { result: Result -> (_binding.sdkImpl as KotlinSDKImplementation).onGooglePayResult(result) }
        )

        _binding.googlePayContext = _googlePayContext

        val toolbar = _binding.toolbarLayout.findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "${supportActionBar!!.title} : Kotlin"

        _binding.cardSingleLineView.cardInputListener = this

        _binding.cardMultiLineView.cardInputListener = this
        _binding.cardDetailsForm.formValidCallback = this
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.test_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> showSettings()
            R.id.restart -> AppUtils.restartApp(this)
        }

        return true
    }

    private fun showSettings() {
        val settingsFragment = getSettingsFragment(supportFragmentManager)
        if (!settingsFragment.isAdded)
            settingsFragment.show(supportFragmentManager, settingsTag)
    }

    private fun getSettingsFragment(fragmentManager: FragmentManager): SettingsFragment {
        val settingsFragment = fragmentManager.findFragmentByTag(settingsTag)
        if (settingsFragment is SettingsFragment)
            return settingsFragment

        return SettingsFragment()
    }

    override fun onFocusChange(field: CardField) {
        logCardInputChange("Card Field Focus Changed: $field")
    }

    override fun onFieldComplete(field: CardField) {
        logCardInputChange("Card Field Complete: $field")
    }

    override fun onInputChanged(isValid: Boolean, invalidFields: Set<CardField>) {
        logCardInputChange("Input Changed: IsValid: $isValid")
    }

    override fun onInputChanged(isValid: Boolean, fieldStates: Map<CardField, ICardFieldState>) {
        logCardInputChange("Input Changed: IsValid: $isValid")
    }

    private fun logCardInputChange(message: String) {
        if (!isBound)
            return

        if (_binding.settings!!.logCardInputChanges.value == false)
            return

        _binding.viewModel!!.logText(message)
    }

    private val isBound : Boolean
        get() = settingsBound && viewModelBound

    private val settingsBound: Boolean
        get() = _binding.settings != null

    private val viewModelBound: Boolean
        get() = _binding.viewModel != null

    override fun onDismiss(dialog: DialogInterface?) {
        //No need to check dialog type... settings is the only dialog used in the app
        logSettings()
    }

    private fun logSettings() {
        if (!isBound)
            return

        _binding.viewModel!!.logSettings(_binding.settings!!)
    }
}