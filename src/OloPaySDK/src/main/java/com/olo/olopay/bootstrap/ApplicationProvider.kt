// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.bootstrap

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.olo.olopay.internal.providers.BaseProvider
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Convenience provider class to initialize the Olo Pay SDK without
 * needing to create an Application subclass
 * <hr class="divider">
 *
 * ### Usage Details:
 *
 * To initialize the Olo Pay SDK (and perhaps other libraries that need
 * the application context) do the following:
 *
 * 1. Add the following provider tag to your manifest
 *     ```
 *     <provider
 *       android:authorities="your.package.name"
 *       android:name="com.olo.olopay.bootstrap.ApplicationProvider"
 *       android:exported="false" />
 *     ```
 * 2. Call [ApplicationProvider.listen]
 *     ```
 *     val InitializeOloPaySDK by lazy {
 *         ApplicationProvider.listen { application ->
 *             OloPayApiInitializer().setup(application.applicationContext, SetupParameters(false))
 *         }
 *     }
 *
 *     class MyActivity : AppCompatActivity() {
 *         init {
 *             InitializeOloPaySDK
 *         }
 *     }
 *     ```
 * <hr class="spacer">
 *
 * #### Important:
 *
 * _This is not the only way to initialize the Olo Pay SDK. See [ApplicationInitializer]
 * for an alternative approach as well_
 */
// Code inspired by:
// - https://proandroiddev.com/your-android-libraries-should-not-ask-an-application-context-51986cc140d4
// - https://github.com/florent37/ApplicationProvider/tree/master/applicationprovider/src/main/java/com/github/florent37/application/provider
open class ApplicationProvider : BaseProvider {
    /** @suppress */
    // Note: this doesn't use primary constructor syntax so we can suppress it from documentation
    constructor() : super()

    /** @suppress */
    override fun onCreate(): Boolean {
        val appContext = context
        if (appContext is Application)
            appInstance = appContext

        return true
    }

    /**
     * Convenience object for Application-related properties and listeners
     */
    companion object {
        internal  val applicationListeners = ConcurrentLinkedQueue<(Application) -> Unit>()

        /**
         * Register a listener to execute code when the application context becomes available
         * @param listener The code to execute when the application context becomes available
         */
        @JvmStatic
        fun listen(listener: (Application) -> Unit) {
            val app = appInstance
            if (app != null) {
                listener(app)
            } else {
                applicationListeners.add(listener)
            }
        }

        /**
         * Convenience property for getting the current Application instance
         */
        @JvmStatic
        val currentApplication: Application?
            get() = appInstance ?: getApplication()

        //Fallback... should only run once per non-default process (which means it should never
        // be called under normal circumstances)
        @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
        private fun getApplication() : Application? {
            val activityThread = Class.forName("android.app.ActivityThread")
            val currentApp = activityThread.getDeclaredMethod("currentApplication").invoke(null) as? Context
            if (currentApp is Application) {
                appInstance = currentApp
            }

            return appInstance
        }
    }

}

@VisibleForTesting
internal var appInstance: Application? = null
    internal set(value) {
        field = value
        if (value == null)
            return

        ApplicationProvider.applicationListeners.forEach { it.invoke(value) }
    }