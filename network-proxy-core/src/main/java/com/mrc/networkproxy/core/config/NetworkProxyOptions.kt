package com.mrc.networkproxy.core.config

data class NetworkProxyOptions(
    val localHost: String = "127.0.0.1",
    val localPort: Int = 2080,
    val localProxyType: LocalProxyType = LocalProxyType.SOCKS,
    val enableSniffing: Boolean = false,
    val logLevel: String = "warn"
)

data class LocalProxyEndpoint(
    val host: String,
    val port: Int,
    val type: LocalProxyType
)

enum class LocalProxyType {
    SOCKS,
    HTTP,
    MIXED
}

fun LocalProxyType.toJavaProxyType(): java.net.Proxy.Type =
    when (this) {
        LocalProxyType.SOCKS,
        LocalProxyType.MIXED -> java.net.Proxy.Type.SOCKS

        LocalProxyType.HTTP -> java.net.Proxy.Type.HTTP
    }
