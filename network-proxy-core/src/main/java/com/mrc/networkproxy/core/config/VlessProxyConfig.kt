package com.mrc.networkproxy.core.config

data class VlessProxyConfig(
    val uuid: String,
    val serverAddress: String,
    val serverPort: Int,
    val name: String? = null,
    val encryption: String = "none",
    val flow: String? = null,
    val security: VlessSecurity = VlessSecurity.None,
    val transport: VlessTransport = VlessTransport.Tcp,
    val rawParameters: Map<String, String> = emptyMap()
)

sealed interface VlessSecurity {
    data object None : VlessSecurity

    data class Tls(
        val serverName: String? = null,
        val alpn: List<String> = emptyList(),
        val fingerprint: String? = null,
        val allowInsecure: Boolean = false
    ) : VlessSecurity

    data class Reality(
        val serverName: String? = null,
        val publicKey: String,
        val shortId: String? = null,
        val fingerprint: String? = null
    ) : VlessSecurity
}

sealed interface VlessTransport {
    data object Tcp : VlessTransport

    data class WebSocket(
        val path: String? = null,
        val host: String? = null
    ) : VlessTransport

    data class Http(
        val path: String? = null,
        val host: String? = null
    ) : VlessTransport

    data class Grpc(
        val serviceName: String? = null
    ) : VlessTransport
}
