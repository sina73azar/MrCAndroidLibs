package com.mrc.networkproxy.core.config

import org.json.JSONArray
import org.json.JSONObject

object SingBoxConfigBuilder {

    fun build(
        vless: VlessProxyConfig,
        endpoint: LocalProxyEndpoint,
        options: NetworkProxyOptions = NetworkProxyOptions()
    ): String {
        return JSONObject()
            .put(
                "log",
                JSONObject()
                    .put("level", options.logLevel)
                    .put("timestamp", true)
            )
            .put("inbounds", JSONArray().put(buildInbound(endpoint, options)))
            .put("outbounds", JSONArray().put(buildVlessOutbound(vless)))
            .put(
                "route",
                JSONObject()
                    .put("final", "proxy")
                    .put("auto_detect_interface", true)
            )
            .toString(2)
    }

    private fun buildInbound(
        endpoint: LocalProxyEndpoint,
        options: NetworkProxyOptions
    ): JSONObject {
        val type = when (endpoint.type) {
            LocalProxyType.SOCKS -> "socks"
            LocalProxyType.HTTP -> "http"
            LocalProxyType.MIXED -> "mixed"
        }

        return JSONObject()
            .put("type", type)
            .put("tag", "app-in")
            .put("listen", endpoint.host)
            .put("listen_port", endpoint.port)
            .put("sniff", options.enableSniffing)
            .put("set_system_proxy", false)
    }

    private fun buildVlessOutbound(vless: VlessProxyConfig): JSONObject {
        val outbound = JSONObject()
            .put("type", "vless")
            .put("tag", "proxy")
            .put("server", vless.serverAddress)
            .put("server_port", vless.serverPort)
            .put("uuid", vless.uuid)
            .put("packet_encoding", "xudp")

        if (vless.encryption.isNotBlank()) {
            outbound.put("encryption", vless.encryption)
        }
        vless.flow?.let { outbound.put("flow", it) }

        buildTls(vless.security)?.let { outbound.put("tls", it) }
        buildTransport(vless.transport)?.let { outbound.put("transport", it) }

        return outbound
    }

    private fun buildTls(security: VlessSecurity): JSONObject? {
        return when (security) {
            VlessSecurity.None -> null
            is VlessSecurity.Tls -> JSONObject()
                .put("enabled", true)
                .putIfNotNull("server_name", security.serverName)
                .putIfNotEmpty("alpn", security.alpn)
                .putIfNotNull("utls", buildUtls(security.fingerprint))
                .put("insecure", security.allowInsecure)

            is VlessSecurity.Reality -> JSONObject()
                .put("enabled", true)
                .putIfNotNull("server_name", security.serverName)
                .putIfNotNull("utls", buildUtls(security.fingerprint))
                .put(
                    "reality",
                    JSONObject()
                        .put("enabled", true)
                        .put("public_key", security.publicKey)
                        .putIfNotNull("short_id", security.shortId)
                )
        }
    }

    private fun buildTransport(transport: VlessTransport): JSONObject? {
        return when (transport) {
            VlessTransport.Tcp -> null
            is VlessTransport.WebSocket -> JSONObject()
                .put("type", "ws")
                .putIfNotNull("path", transport.path)
                .putIfNotNull(
                    "headers",
                    transport.host?.let { host ->
                        JSONObject().put("Host", host)
                    }
                )

            is VlessTransport.Grpc -> JSONObject()
                .put("type", "grpc")
                .putIfNotNull("service_name", transport.serviceName)
        }
    }

    private fun buildUtls(fingerprint: String?): JSONObject? {
        return fingerprint?.takeIf { it.isNotBlank() }?.let {
            JSONObject()
                .put("enabled", true)
                .put("fingerprint", it)
        }
    }

    private fun JSONObject.putIfNotNull(key: String, value: Any?): JSONObject {
        if (value != null) put(key, value)
        return this
    }

    private fun JSONObject.putIfNotEmpty(key: String, values: List<String>): JSONObject {
        if (values.isNotEmpty()) {
            put(key, JSONArray(values))
        }
        return this
    }
}
