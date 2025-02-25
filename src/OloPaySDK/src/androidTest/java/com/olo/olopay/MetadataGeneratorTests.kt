// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay

import android.os.Build
import com.olo.olopay.api.IOloPayApiInitializer
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.data.SdkBuildType
import com.olo.olopay.data.SdkWrapperInfo
import com.olo.olopay.data.SdkWrapperPlatform
import com.olo.olopay.googlepay.GooglePayConfig
import com.olo.olopay.internal.data.MetadataGenerator
import com.olo.olopay.internal.data.PaymentMethodSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MetadataGeneratorTests {
    private val _googlePayCompanyLabel = "Test Company"
    private val _googlePayCountryCode = "US"
    private val _googlePayEnvironment = com.olo.olopay.googlepay.GooglePayEnvironment.Test

    private val _googlePayConfig = GooglePayConfig(
        _googlePayEnvironment,
        _googlePayCompanyLabel,
        _googlePayCountryCode,
    )

    @Before
    fun setup() {
        IOloPayApiInitializer.sdkWrapperInfo = null
    }

    @Test
    fun metadataGenerator_withNativeSingleLineInputSource_containsCreationSourceData() {
        val metadata = getMetadata(PaymentMethodSource.SingleLineInput)
        assertEquals("singleLineInput", metadata["CreationSource"])
        assertNoGooglePayData(metadata)
        assertNoHybridData(metadata)
    }

    @Test
    fun metadataGenerator_withNativeMultiLineInputSource_containsCreationSourceData() {
        val metadata = getMetadata(PaymentMethodSource.MultiLineInput)
        assertEquals("multiLineInput", metadata["CreationSource"])
        assertNoGooglePayData(metadata)
        assertNoHybridData(metadata)
    }

    @Test
    fun metadataGenerator_withNativeFormLineInputSource_containsCreationSourceData() {
        val metadata = getMetadata(PaymentMethodSource.FormInput)
        assertEquals("formInput", metadata["CreationSource"])
        assertNoGooglePayData(metadata)
        assertNoHybridData(metadata)
    }

    @Test
    fun metadataGenerator_withNativeGooglePaySource_containsCreationSourceData() {
        val metadata = getMetadata(_googlePayConfig)
        assertEquals("googlePay", metadata["CreationSource"])
        assertNoHybridData(metadata)
    }

    @Test
    fun metadataGenerator_withNativeGooglePaySource_containsGooglePayData() {
        val metadata = getMetadata(_googlePayConfig)

        assertTrue(metadata.containsKey("DigitalWalletCompanyLabel"))
        assertEquals("Test Company", metadata["DigitalWalletCompanyLabel"])

        assertTrue(metadata.containsKey("GooglePayEnvironment"))
        assertEquals("test", metadata["GooglePayEnvironment"])

        assertTrue(metadata.containsKey("GooglePayCountryCode"))
        assertEquals("US", metadata["GooglePayCountryCode"])
    }

    @Test
    fun metadataGenerator_withHybridSingleLineSource_containsHybridData() {
        IOloPayApiInitializer.sdkWrapperInfo = SdkWrapperInfo(
            1,
            2,
            3,
            SdkBuildType.Internal,
            SdkWrapperPlatform.ReactNative)

        val metadata = getMetadata(PaymentMethodSource.SingleLineInput)
        assertStaticHybridSdkKeysExist(metadata)
        assertNoGooglePayData(metadata)

        assertEquals("1.2.3", metadata["HybridVersion"])
        assertEquals("internal", metadata["HybridBuildType"])
        assertEquals("reactNative", metadata["HybridPlatform"])
    }

    @Test
    fun metadataGenerator_withHybridMultiLineSource_containsHybridData() {
        IOloPayApiInitializer.sdkWrapperInfo = SdkWrapperInfo(
            2,
            3,
            4,
            SdkBuildType.Public,
            SdkWrapperPlatform.Capacitor)

        val metadata = getMetadata(PaymentMethodSource.MultiLineInput)
        assertStaticHybridSdkKeysExist(metadata)
        assertNoGooglePayData(metadata)

        assertEquals("2.3.4", metadata["HybridVersion"])
        assertEquals("public", metadata["HybridBuildType"])
        assertEquals("capacitor", metadata["HybridPlatform"])
    }

    @Test
    fun metadataGenerator_withHybridFormSource_containsHybridData() {
        IOloPayApiInitializer.sdkWrapperInfo = SdkWrapperInfo(
            3,
            4,
            5,
            SdkBuildType.Public,
            SdkWrapperPlatform.Flutter)

        val metadata = getMetadata(PaymentMethodSource.FormInput)
        assertStaticHybridSdkKeysExist(metadata)
        assertNoGooglePayData(metadata)

        assertEquals("3.4.5", metadata["HybridVersion"])
        assertEquals("public", metadata["HybridBuildType"])
        assertEquals("flutter", metadata["HybridPlatform"])
    }

    @Test
    fun metadataGenerator_withHybridGooglePaySource_containsHybridData() {
        IOloPayApiInitializer.sdkWrapperInfo = SdkWrapperInfo(
            4,
            5,
            6,
            SdkBuildType.Internal,
            SdkWrapperPlatform.Capacitor)

        val metadata = getMetadata(PaymentMethodSource.FormInput)
        assertStaticHybridSdkKeysExist(metadata)

        assertEquals("4.5.6", metadata["HybridVersion"])
        assertEquals("internal", metadata["HybridBuildType"])
        assertEquals("capacitor", metadata["HybridPlatform"])
    }

    @Test
    fun metadataGenerator_withNativeGooglePaySource_withoutGooglePayConfig_containsNoGooglePayData() {
        val metadata = getMetadata(PaymentMethodSource.GooglePay)
        assertNoGooglePayData(metadata)
    }

    @Test
    fun metadataGenerator_withTestEnvironment_containsValidEnvironmentData() {
        IOloPayApiInitializer.environment = OloPayEnvironment.Test
        assertEquals("test", getMetadata(PaymentMethodSource.SingleLineInput)["Environment"])
    }

    @Test
    fun metadataGenerator_withProductionEnvironment_containsValidEnvironmentData() {
        IOloPayApiInitializer.environment = OloPayEnvironment.Production
        assertEquals("production", getMetadata(PaymentMethodSource.SingleLineInput)["Environment"])
    }

    private fun getMetadata(source: PaymentMethodSource) : Map<String, String> {
        val metadata = MetadataGenerator(source).invoke()
        assertStaticMetadata(metadata)
        return metadata
    }

    private fun getMetadata(config: GooglePayConfig) : Map<String, String> {
        val metadata = MetadataGenerator(config).invoke()
        assertStaticMetadata(metadata)
        return metadata
    }

    private fun assertStaticMetadata(metadata: Map<String, String>) {
        //These keys should ALWAYS exist... but we will test the values in specific tests
        assertTrue(metadata.containsKey("CreationSource"))
        assertTrue(metadata.containsKey("BuildType"))
        assertTrue(metadata.containsKey("Version"))
        assertTrue(metadata.containsKey("Platform"))
        assertTrue(metadata.containsKey("ApiVersion"))
        assertTrue(metadata.containsKey("Environment"))

        // These values always exist and never vary... let's test them here
        assertEquals(BuildConfig.SDK_BUILD_TYPE.lowercase(), metadata["BuildType"])
        assertEquals(BuildConfig.SDK_VERSION, metadata["Version"])
        assertEquals("android", metadata["Platform"])
        assertEquals(Build.VERSION.SDK_INT.toString(), metadata["ApiVersion"])
    }

    private fun assertStaticHybridSdkKeysExist(metadata: Map<String, String>) {
        assertTrue(metadata.containsKey("HybridBuildType"))
        assertTrue(metadata.containsKey("HybridPlatform"))
        assertTrue(metadata.containsKey("HybridVersion"))
    }

    private fun assertNoGooglePayData(metadata: Map<String, String>) {
        assertFalse(metadata.containsKey("DigitalWalletCompanyLabel"))
        assertFalse(metadata.containsKey("GooglePayEnvironment"))
        assertFalse(metadata.containsKey("GooglePayCountryCode"))
    }

    private fun assertNoHybridData(metadata: Map<String, String>) {
        assertFalse(metadata.containsKey("HybridBuildType"))
        assertFalse(metadata.containsKey("HybridPlatform"))
        assertFalse(metadata.containsKey("HybridVersion"))
    }
}