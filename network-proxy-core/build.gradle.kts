plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.mrc.networkproxy.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            artifactId = providers.gradleProperty("mrc.artifact.networkProxyCore").get()

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Network Proxy Core")
                description.set("Android OkHttp proxy tunnel API foundation for VLESS-backed local proxy engines.")
                url.set("https://github.com/sina73azar/MrCAndroidLibs")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("sina73azar")
                        name.set("sina73azar")
                        url.set("https://github.com/sina73azar")
                    }
                }
                scm {
                    url.set("https://github.com/sina73azar/MrCAndroidLibs")
                    connection.set("scm:git:git://github.com/sina73azar/MrCAndroidLibs.git")
                    developerConnection.set("scm:git:ssh://git@github.com/sina73azar/MrCAndroidLibs.git")
                }
            }
        }
    }
}

android {
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    api(libs.kotlinx.coroutines.android)
    implementation(libs.libbox.android)
}
