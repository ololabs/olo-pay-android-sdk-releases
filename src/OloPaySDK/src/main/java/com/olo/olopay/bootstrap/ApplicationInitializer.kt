// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.bootstrap

import android.app.Application
import com.olo.olopay.internal.providers.BaseProvider

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
 * 1. Subclass [ApplicationInitializer]
 * 2. Override [ApplicationInitializer.initialize] and call [OloPayApiInitializer.setup]
 *     ```
 *     class AppStartupProvider : ApplicationInitializer() {
 *         override fun initialize(): (Application) -> Unit = {
 *             CoroutineScope(Dispatchers.IO).launch {
 *                 OloPayApiInitializer().setup(it.applicationContext, SetupParameters(OloPayEnvironment.Test, false))
 *             }
 *         }
 *     }
 *     ```
 * 3. Add the subclass to AndroidManifest
 *     ```
 *     <provider
 *       android:authorities="your.package.name"
 *       android:name=".AppStartupProvider"
 *       android:exported="false" />
 *     ```
 *
 * <hr class="spacer">
 *
 * #### Important:
 * This is not the only way to initialize the Olo Pay SDK. See [ApplicationProvider]
 * for an alternative approach as well
 *
 * @constructor Creates a new [ApplicationInitializer] instance. There should generally not be a need to call this.
 */
// Code inspired by:
// - https://proandroiddev.com/your-android-libraries-should-not-ask-an-application-context-51986cc140d4
// - https://github.com/florent37/ApplicationProvider/tree/master/applicationprovider/src/main/java/com/github/florent37/application/provider
abstract class ApplicationInitializer : ApplicationProvider() {
    /** @suppress */
    override fun onCreate(): Boolean {
        super.onCreate()
        listen(initialize())
        return true
    }

    /**
     * Perform Olo Pay SDK initialization here
     */
    abstract fun initialize() : (Application) -> Unit
}