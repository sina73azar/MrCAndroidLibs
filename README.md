# MrC Network Logger

MrC Network Logger is a small Android network inspection library for OkHttp based apps. It records requests and responses through an OkHttp interceptor and provides an optional Android UI for browsing captured logs during development.

The project publishes two modules:

- `network-logger-core`: OkHttp interceptor, in-memory log store, models, and text formatting.
- `network-logger-ui`: Activity, Compose screens, and optional floating overlay button for viewing logs.

## Release Lines

This repository keeps two release lines:

| Line | Branch | Artifact IDs | Intended Use |
| --- | --- | --- | --- |
| Modern | `master` | `network-logger-core`, `network-logger-ui` | Current Android and OkHttp projects |
| Legacy | `legacy-okhttp3` | `network-logger-core-legacy`, `network-logger-ui-legacy` | Apps that must stay on the old OkHttp 3.x compatible build |

Use the modern artifacts for new apps. Use the legacy artifacts only when your app cannot move off the legacy dependency stack yet.

## Installation

Current versions:

- Modern: `v0.2.0`
- Legacy: `v0.1.1-legacy`

[![](https://www.jitpack.io/v/sina73azar/MrCAndroidLibs.svg)](https://www.jitpack.io/#sina73azar/MrCAndroidLibs)

### Kotlin DSL

Add JitPack to `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Modern dependency:

```kotlin
dependencies {
    debugImplementation("com.github.sina73azar.MrCAndroidLibs:network-logger-ui:v0.2.0")
}
```

Legacy dependency:

```kotlin
dependencies {
    debugImplementation("com.github.sina73azar.MrCAndroidLibs:network-logger-ui-legacy:v0.1.1-legacy")
}
```

Core-only dependencies:

```kotlin
dependencies {
    debugImplementation("com.github.sina73azar.MrCAndroidLibs:network-logger-core:v0.2.0")
    debugImplementation("com.github.sina73azar.MrCAndroidLibs:network-logger-core-legacy:v0.1.1-legacy")
}
```

### Groovy

Add JitPack to `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Modern dependency:

```groovy
dependencies {
    debugImplementation 'com.github.sina73azar.MrCAndroidLibs:network-logger-ui:v0.2.0'
}
```

Legacy dependency:

```groovy
dependencies {
    debugImplementation 'com.github.sina73azar.MrCAndroidLibs:network-logger-ui-legacy:v0.1.1-legacy'
}
```

Core-only dependencies:

```groovy
dependencies {
    debugImplementation 'com.github.sina73azar.MrCAndroidLibs:network-logger-core:v0.2.0'
    debugImplementation 'com.github.sina73azar.MrCAndroidLibs:network-logger-core-legacy:v0.1.1-legacy'
}
```

The UI module depends on the core module, so most apps only need to add `network-logger-ui`. If you only want the interceptor and stored log stream without the UI, depend on the core artifact instead.

## Basic Usage

Add the interceptor to your OkHttp client:

```kotlin
import com.mrc.networklogger.core.api.NetworkLogger
import okhttp3.OkHttpClient

val okHttpClient = OkHttpClient.Builder()
    .addNetworkInterceptor(NetworkLogger.interceptor)
    .build()
```

Open the logger UI manually:

```kotlin
import com.mrc.networklogger.ui.navigation.LoggerLauncher

LoggerLauncher.open(context)
```

Or install the floating logger button from an `AppCompatActivity`:

```kotlin
import com.mrc.networklogger.ui.fab.installLoggerOverlay

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installLoggerOverlay()
    }
}
```

## Logging Level

The interceptor defaults to body logging. You can change the level at app startup:

```kotlin
import com.mrc.networklogger.core.interceptor.LoggerInterceptor

NetworkLogger.interceptor.level = LoggerInterceptor.Level.BASIC
```

Available levels:

- `NONE`: disables logging.
- `BASIC`: logs request and response lines.
- `HEADERS`: logs request and response lines plus headers.
- `BODY`: logs request and response lines, headers, and text bodies.

## Recommended App Setup

Use this library only in debug or internal builds:

```kotlin
dependencies {
    debugImplementation("com.github.sina73azar.MrCAndroidLibs:network-logger-ui:v0.2.0")
    releaseImplementation("com.github.sina73azar.MrCAndroidLibs:network-logger-core:v0.2.0")
}
```

If you do not want any logger classes in release builds, keep all usage behind your own debug-only source set or build flag.

Avoid logging sensitive production traffic. Body logging can include credentials, tokens, personal data, and request payloads.

## Publishing From This Repo

JitPack builds the project with:

```bash
./gradlew -Dorg.gradle.offline=false clean publishToMavenLocal -x test
```

Modern releases should be tagged from `master`:

```bash
git tag v0.2.0
git push origin v0.2.0
```

Legacy releases should be tagged from `legacy-okhttp3`:

```bash
git checkout legacy-okhttp3
git tag v0.1.1-legacy
git push origin legacy-okhttp3 v0.1.1-legacy
```

## Development

Build all modules:

```bash
./gradlew clean build
```

Publish locally for verification:

```bash
./gradlew publishToMavenLocal
```

The sample `app` module consumes the local project modules and can be used to verify the logger UI and interceptor behavior before tagging a release.
