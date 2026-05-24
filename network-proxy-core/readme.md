# Network Proxy Core

Android library foundation for proxying selected OkHttp and Retrofit calls through a local VLESS-backed sing-box/libbox engine.

This module starts a local proxy listener, usually `127.0.0.1:2080`, and gives the app a regular Java `Proxy` object. The app decides which OkHttp or Retrofit clients use that proxy.

It does not use Android `VpnService` and does not proxy the whole device.

## Structure

- `api/NetworkProxy.kt`
  - Singleton lifecycle entry point.
  - Starts/stops the active libbox proxy engine.
  - Exposes the active `ProxySession`.
  - Provides `start(...)` for one VLESS config and `startFromSubscription(...)` for subscription-driven selection.

- `config/`
  - `VlessUriParser`: parses `vless://...` links into structured config.
  - `SingBoxConfigBuilder`: converts structured config into sing-box JSON.
  - `NetworkProxyOptions`: local listener and sing-box behavior.
  - `VlessSubscriptionOptions`: subscription selection and probe settings.

- `subscription/VlessSubscriptionSelector.kt`
  - Fetches a subscription URL directly.
  - Accepts plain text and common Base64 subscription bodies.
  - Extracts `vless://` links.
  - TCP-probes candidates and sorts by latency.

- `engine/`
  - `ProxyEngine`: abstraction for alternate native engines.
  - `LibboxProxyEngine`: default sing-box/libbox implementation.

## Direct VLESS Usage

Use this when the app already has one VLESS URI or one parsed `VlessProxyConfig`.

```kotlin
val session = NetworkProxy.start(
    context = context,
    vlessUri = vlessUri,
    options = NetworkProxyOptions(
        localHost = "127.0.0.1",
        localPort = 2080,
        logLevel = "debug"
    )
)

val proxiedClient = OkHttpClient.Builder()
    .proxy(session.proxy)
    .build()
```

Use `proxiedClient` only for calls that should go through the tunnel.

## Subscription Usage

Use this when the app has a subscription URL and wants the library to choose a working server.

```kotlin
val session = NetworkProxy.startFromSubscription(
    context = context,
    subscriptionUrl = subscriptionUrl,
    options = NetworkProxyOptions(logLevel = "debug"),
    subscriptionOptions = VlessSubscriptionOptions(
        maxLatencyMs = 1500,
        probeUrl = "https://dummyjson.com/posts"
    )
)
```

The subscription flow:

1. Fetches the subscription URL directly, without using the proxy.
2. Parses plain text or Base64 bodies.
3. Extracts VLESS links.
4. Sorts reachable candidates by TCP latency.
5. Starts libbox with each candidate.
6. Probes `probeUrl` through the local SOCKS proxy.
7. Keeps the first candidate that completes the probe request.

This matters because TCP latency only proves that `server:port` is reachable. It does not prove that the VLESS/TLS/WebSocket handshake works.

## App Integration Example

Keep sensitive subscription URLs out of source control. In an app module, read the value from `local.properties` or CI environment and expose it through `BuildConfig`.

<details open>
<summary>Kotlin DSL</summary>

```kotlin
// app/build.gradle.kts
import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}

val subscriptionUrl =
    localProperties.getProperty("proxy.subscriptionUrl")
        ?: System.getenv("VLESS_SUBSCRIPTION_URL")
        ?: ""

android {
    defaultConfig {
        buildConfigField("String", "VLESS_SUBSCRIPTION_URL", "\"$subscriptionUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }
}
```

</details>

<details>
<summary>Groovy</summary>

```groovy
// app/build.gradle
import java.util.Properties

def localProperties = new Properties()
def localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.withInputStream { stream ->
        localProperties.load(stream)
    }
}

def subscriptionUrl =
        localProperties.getProperty("proxy.subscriptionUrl")
                ?: System.getenv("VLESS_SUBSCRIPTION_URL")
                ?: ""

android {
    defaultConfig {
        buildConfigField "String", "VLESS_SUBSCRIPTION_URL", "\"${subscriptionUrl}\""
    }

    buildFeatures {
        buildConfig true
    }
}
```

</details>

Then the app only needs a small starter:

```kotlin
class AppNetworkProxyStarter(
    private val context: Context
) {
    suspend fun ensureStarted(): ProxySession {
        NetworkProxy.currentSession?.let { return it }

        return NetworkProxy.startFromSubscription(
            context = context,
            subscriptionUrl = BuildConfig.VLESS_SUBSCRIPTION_URL,
            options = NetworkProxyOptions(logLevel = "debug")
        )
    }
}
```

## OkHttp Setup

`network-proxy-core` does not depend on OkHttp. If your app uses OkHttp or Retrofit, configure the client in the app layer.

For a client created after the proxy is already started, a fixed proxy is enough:

```kotlin
val session = NetworkProxy.startFromSubscription(
    context = context,
    subscriptionUrl = BuildConfig.VLESS_SUBSCRIPTION_URL
)

val okHttpClient = OkHttpClient.Builder()
    .proxy(session.proxy)
    .build()
```

If the OkHttp client is created before proxy startup, use a `ProxySelector` that reads the current session at request time:

```kotlin
private fun activeProxySelector(): ProxySelector {
    return object : ProxySelector() {
        override fun select(uri: URI?): List<Proxy> {
            val proxy = NetworkProxy.currentSession?.proxy ?: Proxy.NO_PROXY
            Log.d("NetworkProxyTunnel", "ProxySelector.select uri=$uri proxy=$proxy")
            return listOf(proxy)
        }

        override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
            Log.e("NetworkProxyTunnel", "ProxySelector.connectFailed uri=$uri socketAddress=$sa", ioe)
        }
    }
}
```

Optional tunnel diagnostics:

```kotlin
private fun tunnelLoggingEventListenerFactory(): EventListener.Factory {
    return EventListener.Factory {
        object : EventListener() {
            override fun connectStart(
                call: Call,
                inetSocketAddress: InetSocketAddress,
                proxy: Proxy
            ) {
                Log.d(
                    "NetworkProxyTunnel",
                    "OkHttp connectStart url=${call.request().url} target=$inetSocketAddress proxy=$proxy"
                )
            }
        }
    }
}
```

Apply both to the app's OkHttp client:

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .proxySelector(activeProxySelector())
    .eventListenerFactory(tunnelLoggingEventListenerFactory())
    .build()
```

Make sure requests that must be tunneled call `NetworkProxy.start(...)` or `NetworkProxy.startFromSubscription(...)` before execution. Otherwise the dynamic selector falls back to `Proxy.NO_PROXY`.

## Supported VLESS Fields

Currently supported:

- `security=none`
- `security=tls`
- `security=reality`
- `type=ws`
- `type=grpc`
- `type=tcp`
- `type=tcp&headerType=http`
- `type=xhttp` / `type=splithttp` as a best-effort compatibility mapping to sing-box HTTP transport
- `sni`
- `fp`
- `alpn`
- `allowInsecure`
- `host`
- `path`
- `flow`

Important limitation: Xray XHTTP/SplitHTTP is not the same as sing-box HTTP transport. The compatibility mapping can let the app try those links, but servers that require true XHTTP behavior may still fail.

## Local Proxy Endpoint

The default local endpoint is:

```text
127.0.0.1:2080 SOCKS
```

You can change it with `NetworkProxyOptions`:

```kotlin
NetworkProxyOptions(
    localHost = "127.0.0.1",
    localPort = 2080,
    localProxyType = LocalProxyType.SOCKS,
    enableSniffing = false,
    autoDetectInterface = false
)
```

`autoDetectInterface` defaults to `false` because Android apps may be blocked by SELinux when libbox tries to inspect `/proc/net/tcp`.

## Logging

Useful Android Logcat tags:

- `NetworkProxy`
- `LibboxProxyEngine`
- `VlessSubscriptionSelector`

When `logLevel = "debug"`, the library logs:

- selected VLESS candidate
- generated sing-box JSON
- local proxy endpoint
- tunnel probe result

Avoid logging production subscription URLs or generated configs if they contain sensitive server details.
