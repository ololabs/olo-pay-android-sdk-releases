plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.navigation.safeargs) apply false
}

apply(from = "${rootDir}/scripts/publish-root.gradle")

group = "com.olo.olopay"
version = System.getenv("SDK_PUBLISH_VERSION") ?: "0.0.0"
extra["PUBLISH_GROUP_ID"] = "com.olo.olopay"
extra["PUBLISH_ARTIFACT_ID"] = "olo-pay-android-sdk"
extra["DEV_PUBLISH_ARTIFACT_ID"] = "olo-pay-android-sdk-internal"
extra["LOCAL_DEV_RELEASE_REPO_PATH"] = System.getenv("LOCAL_DEV_RELEASE_REPO") ?: ""

// Needed for the publish scripts
extra["PUBLISH_VERSION"] = version

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}