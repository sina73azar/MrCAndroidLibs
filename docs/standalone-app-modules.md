# Standalone App Modules

This repository can host temporary or product-specific Android app modules alongside the library modules. Use this only when sharing the existing Gradle setup is useful. For large, long-lived apps, prefer a separate repository.

## When To Add A Standalone Module Here

Good fit:

- Urgent APK delivery.
- Demo app for one customer or one experiment.
- Small app that benefits from existing Gradle, Kotlin, Compose, and dependency setup.
- App that may use local library modules while being developed.

Poor fit:

- App has its own long release lifecycle.
- App has many secrets, environments, backend configs, or CI rules.
- App should be owned, versioned, or published separately from this repo.
- App needs a very different technology stack.

## Naming Rules

Use a clear module directory name:

```text
customer_clock/
internal_demo/
proxy_test_app/
```

Use a separate package/application ID:

```kotlin
namespace = "com.company.product"
applicationId = "com.company.product"
```

Do not reuse package names from:

- `app`
- `network-logger-core`
- `network-logger-ui`
- `network-proxy-core`

## Required Files

Minimum structure:

```text
new_app/
  .gitignore
  build.gradle.kts
  proguard-rules.pro
  src/main/AndroidManifest.xml
  src/main/java/<package>/MainActivity.kt
  src/main/res/values/strings.xml
  src/main/res/values/themes.xml
```

Add the module to `settings.gradle.kts`:

```kotlin
include(":new_app")
```

## Module .gitignore

Every standalone app module should have:

```text
/build/
/release/
*.jks
*.keystore
```

This prevents accidental commits of generated APKs and local signing keys.

## Gradle Template

Use this as a starting point:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.newapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.newapp"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

## Optional Local Library Dependencies

If the new app needs the local libraries, prefer project dependencies during development:

```kotlin
dependencies {
    debugImplementation(project(":network-logger-ui"))
    implementation(project(":network-proxy-core"))
}
```

If the app must behave like an external consumer, use published JitPack coordinates instead.

## Release And Signing

Keep signing local unless the project explicitly adds a secure signing setup.

Do not commit:

- `.jks`
- `.keystore`
- generated APKs
- `release/`
- local passwords
- `local.properties`

If signing values are needed, use local Gradle properties or environment variables.

## Checklist For Future App Modules

1. Create a feature branch for the app.
2. Add a new module directory with a unique app name.
3. Add the module to `settings.gradle.kts`.
4. Set independent `namespace` and `applicationId`.
5. Add module-local `.gitignore`.
6. Keep app code independent from library publishing config.
7. Build the module:

```bash
./gradlew :new_app:assembleDebug
```

8. Check ignored output:

```bash
git status --short --ignored new_app
```

9. Commit only source/config/docs.

10. If the app becomes long-lived, plan a move to a separate repository or a dedicated product branch.
