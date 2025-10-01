import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.dokka)
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        // Needed for metadata
        buildConfigField("String", "SDK_VERSION", "\"${rootProject.extra["PUBLISH_VERSION"]}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // The following argument makes the Android Test Orchestrator run its
        // "pm clear" command after each test invocation. This command ensures
        // that the app's state is completely cleared between tests.
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        targetSdk = 36
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    flavorDimensions += "OloPayEnv"
    productFlavors {
        create("Dev") {
            dimension = "OloPayEnv"
            buildConfigField("String", "SDK_BUILD_TYPE", "\"Internal\"")
        }
        create("Prod") {
            dimension = "OloPayEnv"
            buildConfigField("String", "SDK_BUILD_TYPE", "\"Public\"")
        }
    }

    namespace = "com.olo.olopay"
    buildFeatures {
        buildConfig = true
    }
}

apply(from = "${rootProject.projectDir}/scripts/publish-module-shared.gradle")
apply(from = "${rootProject.projectDir}/scripts/publish-module.gradle")
apply(from = "${rootProject.projectDir}/scripts/publish-dev-module.gradle")

tasks.withType<DokkaTask>().configureEach {
    pluginsMapConfiguration.set(
        mapOf(
            "org.jetbrains.dokka.base.DokkaBase" to """{
                "separateInheritedMembers": true
            }"""
        )
    )
}

tasks.named<DokkaTask>("dokkaHtml") {
    dokkaSourceSets {
        named("main") {
            skipDeprecated.set(true)
            includeNonPublic.set(false)
            suppressObviousFunctions.set(true)
            suppressInheritedMembers.set(true)
            skipEmptyPackages.set(true)
            displayName.set("Olo Pay SDK")
            reportUndocumented.set(true)
            includes.from("docs/setup.md", "docs/package-docs.md")
        }
    }
}

// This fixes a build error with newer versions of Gradle... See here for more info:
// https://github.com/Kotlin/dokka/issues/3117#issuecomment-1706535157
afterEvaluate {
    tasks.named("dokkaJavadoc") {
        dependsOn(tasks.named("kaptDevDebugKotlin"), tasks.named("kaptDevReleaseKotlin"), tasks.named("kaptProdReleaseKotlin"))
    }
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.google.material)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.google.play.services.wallet)
    implementation(libs.androidx.lifecycle.extensions)
    api(libs.stripe.android)

    dokkaPlugin(libs.dokka.android.documentation.plugin)

    // Testing Dependencies
    testImplementation(libs.junit)
    testImplementation(libs.json)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestUtil(libs.androidx.test.orchestrator)

}