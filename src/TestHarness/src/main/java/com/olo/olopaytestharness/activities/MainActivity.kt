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
import com.olo.olopaytestharness.olopaysdk.JavaSDKImplementation
import com.olo.olopaytestharness.olopaysdk.KotlinSDKImplementation
import com.olo.olopaytestharness.util.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private var initialized = false
    private lateinit var initializationStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        initializationStatus = findViewById<TextView>(R.id.sdk_initialization_status)

        val javaButton = findViewById<Button>(R.id.java_button)
        javaButton.setOnClickListener {
            if (initialized) {
                launchJavaActivity()
            } else {
                initializeJavaSDK()
            }
        }

        val kotlinButton = findViewById<Button>(R.id.kotlin_button)
        kotlinButton.setOnClickListener {
            if (!initialized) {
                KotlinSDKImplementation.InitializeSDK
                initializationStatus.setText(R.string.sdk_initialization_status_initialized_kotlin)
            }

            val intent = Intent(this@MainActivity, KotlinOloPayTestActivity::class.java)
            startActivity(intent)
        }

        val restartButton = findViewById<Button>(R.id.restart_button)
        restartButton.setOnClickListener {
            AppUtils.restartApp(this)
        }
    }

    fun initializeJavaSDK() {
        JavaSDKImplementation.initializeSDK(object: InitCompleteCallback {
            override fun onComplete() {
                initialized = true

                CoroutineScope(Dispatchers.Main).launch {
                    initializationStatus.setText(R.string.sdk_initialization_status_initialized_java)
                }

                launchJavaActivity()
            }
        })
    }

    fun launchJavaActivity() {
        val intent = Intent(this@MainActivity, JavaOloPayTestActivity::class.java)
        startActivity(intent)
    }
}