// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavArgument
import androidx.navigation.NavType
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.olo.olopaytestharness.R
import com.olo.olopaytestharness.databinding.ActivityOloPayTestBinding
import com.olo.olopaytestharness.fragments.SettingsFragment
import com.olo.olopaytestharness.models.SettingsType
import com.olo.olopaytestharness.util.AppUtils
import kotlinx.serialization.ExperimentalSerializationApi

@ExperimentalSerializationApi
class OloPayTestActivity : AppCompatActivity() {
    private val useKotlin
        get() = intent.getBooleanExtra(UseKotlinKey, true)

    private var currentTabId: Int = R.id.card_input_fragment

    private val currentSettingsType: SettingsType
        get() = when (currentTabId) {
            R.id.card_input_fragment -> SettingsType.CreditCard
            R.id.google_pay_fragment -> SettingsType.GooglePay
            else -> SettingsType.CvvToken
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityOloPayTestBinding = DataBindingUtil.setContentView(this, R.layout.activity_olo_pay_test)
        binding.lifecycleOwner = this

        val toolbar = binding.toolbarLayout.findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val platformName = if (useKotlin) "Kotlin" else "Java"
        supportActionBar!!.title = "${supportActionBar!!.title} : $platformName"

        val navController = findNavController(R.id.tab_content_container)

        //Set up fragments to use a Kotlin or Java Olo Pay SDK Implementation
        val navGraph = navController.navInflater.inflate(R.navigation.app_navigation)
        val navArgument = NavArgument.Builder().setType(NavType.BoolType).setDefaultValue(useKotlin).build()
        val navDestinations = navGraph.nodes
        for (i in 0..navDestinations.size()) {
            val key = navDestinations.keyAt(i)
            navDestinations.get(key)?.addArgument(UseKotlinKey, navArgument)
        }

        navController.graph = navGraph
        binding.tabNavigation.setupWithNavController(navController)

        binding.tabNavigation.setOnItemSelectedListener {
            currentTabId = it.itemId
            onNavDestinationSelected(it, navController)
            true
        }

        addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.test_activity_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.show_settings -> showSettings()
                    R.id.restart -> AppUtils.restartApp(this@OloPayTestActivity)
                }

                return true
            }
        })
    }

    private fun showSettings() {
        val settingsFragment = getSettingsFragment(supportFragmentManager)
        settingsFragment.settingsType = currentSettingsType
        if (!settingsFragment.isAdded) {
            settingsFragment.show(supportFragmentManager, SettingsDialogTag)
        }
    }

    private fun getSettingsFragment(fragmentManager: FragmentManager): SettingsFragment {
        val settingsFragment = fragmentManager.findFragmentByTag(SettingsDialogTag)
        if (settingsFragment is SettingsFragment)
            return settingsFragment

        return SettingsFragment()
    }

    companion object {
        const val UseKotlinKey = "useKotlin"
        private const val SettingsDialogTag = "SettingsDialog"
    }
}