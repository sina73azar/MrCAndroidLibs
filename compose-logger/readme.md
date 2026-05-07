````md
# Compose Logger

A lightweight Chucker-like network inspector for Android with:

- Jetpack Compose support
- Internal logger UI
- Public OkHttp interception API
- XML + Compose floating debug button
- Zero required DI setup
- Kotlin-first architecture

---

# Features

- ✅ OkHttp network interception
- ✅ Request / response inspection
- ✅ Headers & body viewer
- ✅ Compose support
- ✅ XML/View-system support
- ✅ Internal logger screen
- ✅ Floating debug FAB
- ✅ cURL generation utilities
- ✅ Lightweight architecture
- ✅ No mandatory Hilt/DI integration
- ✅ Easy plug-and-play setup

---

# Architecture

```text
OkHttp Interceptor
        ↓
LoggerStore (singleton state holder)
        ↓
LoggerViewModel
        ↓
LoggerScreen
        ↓
LoggerActivity
````

---

# Installation

## 1. Add dependency

```kotlin
dependencies {
    implementation("your.group:compose-logger:x.x.x")
}
```

---

# Setup

## Add interceptor to OkHttp

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(ComposeLogger.interceptor)
    .build()
```

Or manually:

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(LoggerInterceptor())
    .build()
```

---

# Compose Integration

Add `LoggerFab()` somewhere near your root UI.

Example:

```kotlin
MaterialTheme {
    content()

    LoggerFab()
}
```

Or inside your custom app theme:

```kotlin
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme {

        content()

        LoggerFab()
    }
}
```

---

# XML / View-System Integration

Inside your activity:

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installLoggerOverlay()
    }
}
```

---

# Public API

```text
ComposeLogger
LoggerInterceptor
LoggerFab
installLoggerOverlay()
LoggerActivity
```

---

# Internal Components

```text
LoggerGraph
LoggerStore
LoggerViewModel
LoggerScreen
```

---

# Philosophy

This SDK intentionally avoids forcing architectural decisions on host applications.

No required:

* Hilt
* Koin
* ViewModel injection
* DI graph setup

The library manages its own internal singleton graph.

---

# Example

## Compose App

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(ComposeLogger.interceptor)
    .build()

LoggerFab()
```

---

## XML App

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(ComposeLogger.interceptor)
    .build()

installLoggerOverlay()
```

---

# Logger UI

The logger screen includes:

* Request list
* Response details
* Status codes
* Duration
* Headers
* Body viewer
* Search/filter support

---

# Future Roadmap

Planned features:

* [ ] Export logs
* [ ] Share logs
* [ ] Room persistence
* [ ] Request replay
* [ ] WebSocket inspector
* [ ] Network timeline visualization
* [ ] HAR export
* [ ] Advanced filtering
* [ ] Log retention policies

---

# Performance Notes

Current implementation uses:

```kotlin
_networkLogs.update { it + entry }
```

This is acceptable for moderate usage.

Future optimizations may include:

* Ring buffer
* Persistent collections
* Max log limits
* Batched state updates

---

# Why This Library?

Most Android network inspectors:

* are tightly coupled to DI
* do not support Compose well
* require complex setup
* are difficult to customize

Compose Logger aims to provide:

* minimal setup
* modern Kotlin APIs
* clean Compose integration
* lightweight runtime overhead

---

# License

MIT License

---

# Contributing

PRs, suggestions, and improvements are welcome.

```
```
