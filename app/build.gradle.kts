import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun String.toBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val vlessSubscriptionUrl =
    (localProperties.getProperty("proxy.subscriptionUrl") ?: System.getenv("VLESS_SUBSCRIPTION_URL")).orEmpty()

android {
    namespace = "com.mrc.MrCAndroidLibs"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mrc.MrCAndroidLibs"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "VLESS_SUBSCRIPTION_URL", vlessSubscriptionUrl.toBuildConfigString())
        buildConfigField("int", "VLESS_ACCEPTABLE_LATENCY_MS", "1500")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    /*kotlinOptions {
        jvmTarget = "11"
    }*/
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    /**
     * Test
     * */
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    /**
     * Debug
     * */
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    /**
     * Koin
     * we better get it once and cache it because repo is unaccessible with this internet
     * */
    /**     val koin_version = "4.2"
    implementation(platform("io.insert-koin:koin-bom:$koin_version"))
    implementation("io.insert-koin:koin-android")
    implementation("io.insert-koin:koin-compose")
    implementation("io.insert-koin:koin-compose-viewmodel")
     */

    /**
     * Hilt
     * */
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    /**
     * Network
     * */
    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    /**
     * Serialization & Convertor
     * */
    implementation(libs.kotlinx.serialization.json)

    /**
     *
     * */
    implementation(libs.kotlinx.coroutines.android)

    implementation("com.github.sina73azar.MrCAndroidLibs:network-logger-ui:v0.2.0")
    implementation(project(":network-proxy-core"))
}
