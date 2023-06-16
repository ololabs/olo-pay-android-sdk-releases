package com.olo.olopay

import android.os.Build
import com.olo.olopay.api.IOloPayApiInitializer
import com.olo.olopay.data.OloPayEnvironment
import com.olo.olopay.data.SdkWrapperBuildType
import com.olo.olopay.data.SdkWrapperInfo
import com.olo.olopay.data.SdkWrapperPlatform
import com.olo.olopay.googlepay.Config
import com.olo.olopay.internal.data.MetadataGenerator
import com.olo.olopay.internal.data.PaymentMethodSource
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MetadataGeneratorTests {
    private val _googlePayCompanyLabel = "Test"
    private val _googlePayCountryCode = "US"
    private val _googlePayEnvironment = com.olo.olopay.googlepay.Environment.Test

    @Before
    fun setup() {
        IOloPayApiInitializer.sdkWrapperInfo = null
        IOloPayApiInitializer.googlePayConfig =
            Config(_googlePayEnvironment, _googlePayCompanyLabel, _googlePayCountryCode)
    }

    @Test
    fun metadataGenerator_withNativeSingleLineInputSource_containsCreationSourceData() {
        val metadata = getMetadata(PaymentMethodSource.SingleLineInput)
        assertEquals(PaymentMethodSource.SingleLineInput.toString(), metadata["CreationSource"])
        assertNoGooglePayData(metadata)
        assertNoHybridData(metadata)
    }

    @Test
    fun metadataGenerator_withNativeMultiLineInputSource_containsCreationSourceData() {
        val metadata = getMetadata(PaymentMethodSource.MultiLineInput)
        assertEquals(PaymentMethodSource.MultiLineInput.toString(), metadata["CreationSource"])
        assertNoGooglePayData(metadata)
        assertNoHybridData(metadata)
    }

    @Test
    fun metadataGenerator_withNativeFormLineInputSource_containsCreationSourceData() {
        val metadata = getMetadata(PaymentMethodSource.FormInput)
        assertEquals(PaymentMethodSource.FormInput.toString(), metadata["CreationSource"])
        assertNoGooglePayData(metadata)
        assertNoHybridData(metadata)
    }

    @Test
    fun metadataGenerator_withNativeGooglePaySource_containsCreationSourceData() {
        val metadata = getMetadata(PaymentMethodSource.GooglePay)
        assertEquals(metadata["CreationSource"], PaymentMethodSource.GooglePay.toString())
        assertNoHybridData(metadata)
    }

    @Test
    fun metadataGenerator_withNativeGooglePaySource_containsGooglePayData() {
        val metadata = getMetadata(PaymentMethodSource.GooglePay)

        assertTrue(metadata.containsKey("DigitalWalletCompanyLabel"))
        assertEquals(_googlePayCompanyLabel, metadata["DigitalWalletCompanyLabel"])

        assertTrue(metadata.containsKey("GooglePayEnvironment"))
        assertEquals(_googlePayEnvironment.toString(), metadata["GooglePayEnvironment"])

        assertTrue(metadata.containsKey("GooglePayCountryCode"))
        assertEquals(_googlePayCountryCode, metadata["GooglePayCountryCode"])
    }

    @Test
    fun metadataGenerator_withHybridSingleLineSource_containsHybridData() {
        IOloPayApiInitializer.sdkWrapperInfo = SdkWrapperInfo(
            1,
            2,
            3,
            SdkWrapperBuildType.Internal,
            SdkWrapperPlatform.ReactNative)

        val metadata = getMetadata(PaymentMethodSource.SingleLineInput)
        assertStaticHybridSdkKeysExist(metadata)
        assertNoGooglePayData(metadata)

        assertEquals("1.2.3", metadata["HybridVersion"])
        assertEquals("Internal", metadata["HybridBuildType"])
        assertEquals("ReactNative", metadata["HybridPlatform"])
    }

    @Test
    fun metadataGenerator_withHybridMultiLineSource_containsHybridData() {
        IOloPayApiInitializer.sdkWrapperInfo = SdkWrapperInfo(
            2,
            3,
            4,
            SdkWrapperBuildType.Public,
            SdkWrapperPlatform.Capacitor)

        val metadata = getMetadata(PaymentMethodSource.MultiLineInput)
        assertStaticHybridSdkKeysExist(metadata)
        assertNoGooglePayData(metadata)

        assertEquals("2.3.4", metadata["HybridVersion"])
        assertEquals("Public", metadata["HybridBuildType"])
        assertEquals("Capacitor", metadata["HybridPlatform"])
    }

    @Test
    fun metadataGenerator_withHybridFormSource_containsHybridData() {
        IOloPayApiInitializer.sdkWrapperInfo = SdkWrapperInfo(
            3,
            4,
            5,
            SdkWrapperBuildType.Public,
            SdkWrapperPlatform.ReactNative)

        val metadata = getMetadata(PaymentMethodSource.FormInput)
        assertStaticHybridSdkKeysExist(metadata)
        assertNoGooglePayData(metadata)

        assertEquals("3.4.5", metadata["HybridVersion"])
        assertEquals("Public", metadata["HybridBuildType"])
        assertEquals("ReactNative", metadata["HybridPlatform"])
    }

    @Test
    fun metadataGenerator_withHybridGooglePaySource_containsHybridData() {
        IOloPayApiInitializer.sdkWrapperInfo = SdkWrapperInfo(
            4,
            5,
            6,
            SdkWrapperBuildType.Internal,
            SdkWrapperPlatform.Capacitor)

        val metadata = getMetadata(PaymentMethodSource.FormInput)
        assertStaticHybridSdkKeysExist(metadata)

        assertEquals("4.5.6", metadata["HybridVersion"])
        assertEquals("Internal", metadata["HybridBuildType"])
        assertEquals("Capacitor", metadata["HybridPlatform"])
    }

    @Test
    fun metadataGenerator_withNativeGooglePaySource_withoutGooglePayConfig_containsNoGooglePayData() {
        IOloPayApiInitializer.googlePayConfig = null
        val metadata = getMetadata(PaymentMethodSource.GooglePay)
        assertNoGooglePayData(metadata)
    }

    @Test
    fun metadataGenerator_withTestEnvironment_containsValidEnvironmentData() {
        IOloPayApiInitializer.environment = OloPayEnvironment.Test
        assertEquals("Test", getMetadata(PaymentMethodSource.SingleLineInput)["Environment"])
    }

    @Test
    fun metadataGenerator_withProductionEnvironment_containsValidEnvironmentData() {
        IOloPayApiInitializer.environment = OloPayEnvironment.Production
        assertEquals("Production", getMetadata(PaymentMethodSource.SingleLineInput)["Environment"])
    }

    @Test
    fun metadataGenerator_withFreshSetup_containsValidSetupData() {
        IOloPayApiInitializer.freshSetup = true
        assertEquals("true", getMetadata(PaymentMethodSource.SingleLineInput)["FreshInstall"])
    }

    @Test
    fun metadataGenerator_withoutFreshSetup_containsValidSetupData() {
        IOloPayApiInitializer.freshSetup = false
        assertEquals("false", getMetadata(PaymentMethodSource.SingleLineInput)["FreshInstall"])
    }

    private fun getMetadata(source: PaymentMethodSource) : Map<String, String> {
        val metadata = MetadataGenerator(source).invoke()
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
        assertTrue(metadata.containsKey("FreshInstall"))

        // These values always exist and never vary... let's test them here
        assertEquals(BuildConfig.SDK_BUILD_TYPE, metadata["BuildType"])
        assertEquals(BuildConfig.SDK_VERSION, metadata["Version"])
        assertEquals("Android", metadata["Platform"])
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