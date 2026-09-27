import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Release signing values live in the ignored android/key.properties (Task 16).
val keystoreProperties = Properties().apply {
    val file = rootProject.file("key.properties")
    if (file.isFile) file.inputStream().use { load(it) }
}

android {
    namespace = "jp.jacky.meow"
    compileSdk = 36

    defaultConfig {
        applicationId = "jp.jacky.meow"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
            storeFile = keystoreProperties.getProperty("storeFile")?.let { file(it) }
            storePassword = keystoreProperties.getProperty("storePassword")
        }
    }

    buildTypes {
        debug {
            // Google's sample ids: never real traffic. An empty key means purchases never count in debug.
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "PLAY_LICENSE_KEY", "\"\"")
            buildConfigField("boolean", "CONSENT_DEBUG_EEA", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            // The AdMob ids arrive in Task 15. The Play licensing key is public (Play Console > Monetization setup).
            manifestPlaceholders["admobAppId"] = "ADMOB_APP_ID_PENDING"
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ADMOB_BANNER_UNIT_PENDING\"")
            buildConfigField("String", "PLAY_LICENSE_KEY", "\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2xobxWn5frR866t52gilSN1QK3qTlCrN7TiT0WGNKaCrv7B2bTsyY42xkC7BGGygqKSfBuZm68aqzS+f/PsBFWnm6wH9MLvHVDW/grg7SKUAF7y02Gc6SCrAcPZPU8c/+RmDL0IyeMhWfOr1w+0r0QZ2AKsheiHxctfaMgDClnO0cJUooEXozvPnnhJNDx2CbXzhslfTVqJLLK6421f1T77hRjJXy88hldo7HdjoKOmaKn7nnJl+JmGEaGL9llXhaC5oENZ4Az044R7dm1HwCzGaIUTl7zDRqG69rNhyUOEahNpdl0b6kYfLHUW31Iu1vdLEnyEXpyTfTzdwGiKqKQIDAQAB\"")
            buildConfigField("boolean", "CONSENT_DEBUG_EEA", "false")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        // JVM tests may construct android.app.Activity and call android.util.Log; the stubs return defaults.
        unitTests.isReturnDefaultValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("com.google.android.gms:play-services-ads:25.5.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
    implementation("com.android.billingclient:billing-ktx:9.1.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")

    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst {
        check(listOf("keyAlias", "keyPassword", "storeFile", "storePassword").all {
            !keystoreProperties.getProperty(it).isNullOrBlank()
        }) { "Set all release signing values in android/key.properties." }
        check(file(keystoreProperties.getProperty("storeFile")).isFile) {
            "The Android upload keystore does not exist."
        }
    }
}
