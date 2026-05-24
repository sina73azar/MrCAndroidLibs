# MrC Network Logger

MrC Network Logger is a small Android network inspection library for OkHttp based apps. It records requests and responses through an OkHttp interceptor and provides an optional Android UI for browsing captured logs during development.

The project publishes two modules:

- `network-logger-core`: OkHttp interceptor, in-memory log store, models, and text formatting.
- `network-logger-ui`: Activity, Compose screens, and optional floating overlay button for viewing logs.

The repository also contains an experimental module:

- `network-proxy-core`: Android API foundation for proxying selected OkHttp and Retrofit clients through a local VLESS-backed sing-box/libbox proxy engine.

## Release Lines

This repository keeps two release lines:

| Line | Version | Branch | Artifact IDs | Intended Use |
| --- | --- | --- | --- | --- |
| Modern logger | [![v0.2.0](https://img.shields.io/badge/JitPack-v0.2.0-blue)](https://jitpack.io/#sina73azar/MrCAndroidLibs/v0.2.0) | `master` | `network-logger-core`, `network-logger-ui` | Current Android and OkHttp projects |
| Legacy logger | [![v0.1.1-legacy](https://img.shields.io/badge/JitPack-v0.1.1--legacy-blue)](https://jitpack.io/#sina73azar/MrCAndroidLibs/v0.1.1-legacy) | `legacy-okhttp3` | `network-logger-core-legacy`, `network-logger-ui-legacy` | Apps that must stay on the old OkHttp 3.x compatible build |
| Proxy experimental | [![v0.3.0-proxy-alpha01](https://img.shields.io/badge/JitPack-v0.3.0--proxy--alpha01-orange)](https://jitpack.io/#sina73azar/MrCAndroidLibs/v0.3.0-proxy-alpha01) | `master` | `network-proxy-core` | Experimental selected-request VLESS proxying |

Use the modern artifacts for new apps. Use the legacy artifacts only when your app cannot move off the legacy dependency stack yet.

## Installation

The dependency examples below use the versions listed in the release-line table.

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

Experimental proxy dependency:

```kotlin
dependencies {
    implementation("com.github.sina73azar.MrCAndroidLibs:network-proxy-core:v0.3.0-proxy-alpha01")
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

Experimental proxy dependency:

```groovy
dependencies {
    implementation 'com.github.sina73azar.MrCAndroidLibs:network-proxy-core:v0.3.0-proxy-alpha01'
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
}
```

If you only need the interceptor and stored log stream without the UI, use the core artifact in debug builds:

```kotlin
dependencies {
    debugImplementation("com.github.sina73azar.MrCAndroidLibs:network-logger-core:v0.2.0")
}
```

Keep logger usage behind your own debug-only source set or build flag if your release source set must compile without logger classes.

Avoid logging sensitive production traffic. Body logging can include credentials, tokens, personal data, and request payloads.

## Experimental Network Proxy Core

`network-proxy-core` is intended for Android apps that need to proxy selected OkHttp or Retrofit calls through a local VLESS-backed sing-box/libbox proxy engine.

It provides:

- Parses `vless://...` links.
- Builds sing-box compatible JSON.
- Provides a singleton `NetworkProxy` lifecycle API.
- Starts a local SOCKS/HTTP/mixed proxy endpoint.
- Can select and probe working VLESS candidates from a subscription URL.
- Provides a pluggable `ProxyEngine` interface.
- Starts a sing-box engine through `net.clever-vpn:libbox-android`.

The default engine runs a local proxy only. It does not request Android `VpnService` permission and does not proxy whole-device traffic.

Detailed structure, usage examples, subscription setup, and current protocol limitations are documented in [`network-proxy-core/readme.md`](network-proxy-core/readme.md).
