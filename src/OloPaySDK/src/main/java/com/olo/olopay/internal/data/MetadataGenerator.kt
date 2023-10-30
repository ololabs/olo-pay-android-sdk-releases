// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.internal.data

import android.os.Build
import com.olo.olopay.BuildConfig
import com.olo.olopay.api.IOloPayApiInitializer

internal data class MetadataGenerator constructor(internal val source: PaymentMethodSource) {
    internal operator fun invoke(): Map<String, String> {
        val map = mutableMapOf(
            CREATION_SOURCE_KEY to source.toString(),
            SDK_BUILD_TYPE_KEY to BuildConfig.SDK_BUILD_TYPE,
            SDK_VERSION_KEY to BuildConfig.SDK_VERSION,
            SDK_PLATFORM_KEY to SDK_PLATFORM_VALUE,
            ANDROID_API_VERSION_KEY to Build.VERSION.SDK_INT.toString(),
            SDK_ENVIRONMENT_KEY to IOloPayApiInitializer.environment.toString(),
            SDK_FRESH_INSTALL_KEY to IOloPayApiInitializer.freshSetup.toString()
        )

        if (source == PaymentMethodSource.GooglePay) {
            IOloPayApiInitializer.googlePayConfig?.let { config ->
                map.putAll(mapOf(
                    DIGITAL_WALLET_COMPANY_LABEL_KEY to config.companyName,
                    GOOGLE_PAY_ENVIRONMENT_KEY to config.environment.toString(),
                    GOOGLE_PAY_COUNTRY_CODE_KEY to config.companyCountryCode
                ))
            }
        }

        IOloPayApiInitializer.sdkWrapperInfo?.let { hybridInfo ->
            map.putAll(mapOf(
                HYBRID_SDK_PLATFORM_KEY to hybridInfo.platform,
                HYBRID_SDK_VERSION_KEY to hybridInfo.version,
                HYBRID_SDK_BUILD_TYPE_KEY to hybridInfo.buildType
            ))
        }

        return map
    }

    internal companion object {
        // THESE KEYS EXIST FOR ALL GENERATED METADATA
        const val CREATION_SOURCE_KEY = "CreationSource"
        const val SDK_BUILD_TYPE_KEY = "BuildType"
        const val SDK_VERSION_KEY = "Version"
        const val SDK_PLATFORM_KEY = "Platform"
        const val ANDROID_API_VERSION_KEY = "ApiVersion"
        const val SDK_ENVIRONMENT_KEY = "Environment"
        const val SDK_FRESH_INSTALL_KEY = "FreshInstall"

        // THESE KEYS ONLY EXIST IF HYBRID SDK DATA IS SET
        const val HYBRID_SDK_BUILD_TYPE_KEY = "HybridBuildType"
        const val HYBRID_SDK_PLATFORM_KEY = "HybridPlatform"
        const val HYBRID_SDK_VERSION_KEY = "HybridVersion"

        // THESE KEYS ONLY EXIST IF THE SOURCE IS GOOGLE PAY
        const val DIGITAL_WALLET_COMPANY_LABEL_KEY = "DigitalWalletCompanyLabel"
        const val GOOGLE_PAY_ENVIRONMENT_KEY = "GooglePayEnvironment"
        const val GOOGLE_PAY_COUNTRY_CODE_KEY = "GooglePayCountryCode"

        const val SDK_PLATFORM_VALUE = "Android"
    }
}