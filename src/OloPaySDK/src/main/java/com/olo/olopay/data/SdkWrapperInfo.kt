// Copyright © 2022 Olo Inc. All rights reserved.
// This software is made available under the Olo Pay SDK License (See LICENSE.md file)
package com.olo.olopay.data

/** @suppress */
data class SdkWrapperInfo constructor(
    private val majorVersion: Int,
    private val minorVersion: Int,
    private val buildVersion: Int,
    private val sdkBuildType: SdkBuildType,
    private val sdkPlatform: SdkWrapperPlatform
) {

    /** @suppress */
    val version
        get() = "${majorVersion}.${minorVersion}.${buildVersion}"

    /** @suppress */
    val buildType
        get() = sdkBuildType.description

    /** @suppress */
    val platform
        get() = sdkPlatform.description
}