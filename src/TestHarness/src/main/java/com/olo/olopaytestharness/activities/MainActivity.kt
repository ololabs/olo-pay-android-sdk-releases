// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopaytestharness.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.olo.olopay.api.InitCompleteCallback
import com.olo.olopaytestharness.R
import com.olo.olopaytestharness.olopaysdk.JavaOloPayImplementation
import com.olo.olopaytestharness.olopaysdk.KotlinOloPayImplementation
import com.olo.olopaytestharness.util.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi

class MainActivity : AppCompatActivity() {
    private var initialized = false
    private lateinit var initializationStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        initializationStatus = findViewById(R.id.sdk_initialization_status)

        val javaButton = findViewById<Button>(R.id.java_button)
        javaButton.setOnClickListener {
            initializeJavaSDK()
        }

        val kotlinButton = findViewById<Button>(R.id.kotlin_button)
        kotlinButton.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch { initializeKotlinSDK() }
        }

        val restartButton = findViewById<Button>(R.id.restart_button)
        restartButton.setOnClickListener {
            AppUtils.restartApp(this)
        }
    }

    private fun initializeJavaSDK() {
        if (!initialized) {
            initializationStatus.setText(R.string.sdk_initialization_status_initializing)
            JavaOloPayImplementation.initializeSdk(object: InitCompleteCallback {
                override fun onComplete() {
                    initialized = true
                    CoroutineScope(Dispatchers.Main).launch {
                        initializationStatus.setText(R.string.sdk_initialization_status_initialized_java)
                    }

                    launchOloPayActivity(false)
                }
            })
        } else {
            launchOloPayActivity(false)
        }
    }

    private suspend fun initializeKotlinSDK() {
        if (!initialized) {
            initializationStatus.setText(R.string.sdk_initialization_status_initializing)
            KotlinOloPayImplementation.initializeSdk(this)
            initializationStatus.setText(R.string.sdk_initialization_status_initialized_kotlin)
            initialized = true
        }

        launchOloPayActivity(true)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun launchOloPayActivity(useKotlin: Boolean) {
        val intent = Intent(this@MainActivity, OloPayTestActivity::class.java)
        intent.putExtra(OloPayTestActivity.UseKotlinKey, useKotlin)
        startActivity(intent)
    }
}