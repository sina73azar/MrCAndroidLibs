package com.mrc.networkproxy.core.config

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object VlessUriParser {

    fun parse(uri: String): VlessProxyConfig {
        val parsed = URI(uri.trim())
        require(parsed.scheme.equals("vless", ignoreCase = true)) {
            "Only vless:// links are supported."
        }

        val query = parseQuery(parsed.rawQuery)
        val uuid = parsed.rawUserInfo?.decodeUrl().orEmpty()
        require(uuid.isNotBlank()) { "VLESS UUID is missing." }

        val serverAddress = parsed.host
            ?: throw IllegalArgumentException("VLESS server address is missing.")
        val security = parseSecurity(query)
        val serverPort = if (parsed.port > 0) parsed.port else defaultPort(security)

        return VlessProxyConfig(
            uuid = uuid,
            serverAddress = serverAddress,
            serverPort = serverPort,
            name = parsed.rawFragment?.decodeUrl(),
            encryption = query["encryption"] ?: "none",
            flow = query["flow"]?.takeIf { it.isNotBlank() },
            security = security,
            transport = parseTransport(query),
            rawParameters = query
        )
    }

    private fun parseSecurity(query: Map<String, String>): VlessSecurity {
        return when (query["security"]?.lowercase()) {
            "reality" -> VlessSecurity.Reality(
                serverName = query["sni"]?.takeIf { it.isNotBlank() },
                publicKey = query["pbk"]
                    ?: throw IllegalArgumentException("Reality VLESS config requires pbk."),
                shortId = query["sid"]?.takeIf { it.isNotBlank() },
                fingerprint = query["fp"]?.takeIf { it.isNotBlank() }
            )

            "tls" -> VlessSecurity.Tls(
                serverName = query["sni"]?.takeIf { it.isNotBlank() },
                alpn = query["alpn"]
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    .orEmpty(),
                fingerprint = query["fp"]?.takeIf { it.isNotBlank() },
                allowInsecure = query["allowInsecure"] == "1" || query["allowInsecure"] == "true"
            )

            else -> VlessSecurity.None
        }
    }

    private fun parseTransport(query: Map<String, String>): VlessTransport {
        return when (query["type"]?.lowercase()) {
            "ws", "websocket" -> VlessTransport.WebSocket(
                path = query["path"]?.takeIf { it.isNotBlank() },
                host = query["host"]?.takeIf { it.isNotBlank() }
            )

            "grpc" -> VlessTransport.Grpc(
                serviceName = query["serviceName"]?.takeIf { it.isNotBlank() }
            )

            else -> VlessTransport.Tcp
        }
    }

    private fun defaultPort(security: VlessSecurity): Int =
        when (security) {
            VlessSecurity.None -> 80
            is VlessSecurity.Reality,
            is VlessSecurity.Tls -> 443
        }

    private fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrBlank()) return emptyMap()
        return rawQuery.split("&")
            .mapNotNull { part ->
                val separator = part.indexOf("=")
                if (separator < 0) {
                    part.decodeUrl() to ""
                } else {
                    val key = part.substring(0, separator).decodeUrl()
                    val value = part.substring(separator + 1).decodeUrl()
                    key to value
                }
            }
            .toMap()
    }

    private fun String.decodeUrl(): String =
        URLDecoder.decode(this, StandardCharsets.UTF_8.name())
}
