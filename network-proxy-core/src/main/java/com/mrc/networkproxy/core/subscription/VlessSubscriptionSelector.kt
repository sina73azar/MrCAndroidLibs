package com.mrc.networkproxy.core.subscription

import android.os.SystemClock
import android.util.Base64
import android.util.Log
import com.mrc.networkproxy.core.config.SelectedVlessServer
import com.mrc.networkproxy.core.config.VlessProxyConfig
import com.mrc.networkproxy.core.config.VlessSubscriptionOptions
import com.mrc.networkproxy.core.config.VlessUriParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.net.URL
import java.nio.charset.StandardCharsets

class VlessSubscriptionSelector {

    suspend fun select(
        subscriptionUrl: String,
        options: VlessSubscriptionOptions = VlessSubscriptionOptions()
    ): SelectedVlessServer =
        selectCandidates(subscriptionUrl, options).firstOrNull()
            ?: throw IllegalStateException(
                "No VLESS server responded within ${options.maxLatencyMs}ms."
            )

    suspend fun selectCandidates(
        subscriptionUrl: String,
        options: VlessSubscriptionOptions = VlessSubscriptionOptions()
    ): List<SelectedVlessServer> = withContext(Dispatchers.IO) {
        require(subscriptionUrl.isNotBlank()) { "VLESS subscription URL is blank." }

        val body = fetchSubscription(subscriptionUrl, options)
        val candidates = extractVlessLinks(body)
        require(candidates.isNotEmpty()) {
            "Subscription did not contain any vless:// links."
        }

        measureCandidates(
            links = candidates,
            options = options
        ).sortedBy { it.latencyMs }.ifEmpty {
            throw IllegalStateException(
                "No VLESS server responded within ${options.maxLatencyMs}ms. " +
                    "Tested ${candidates.size} subscription entries."
            )
        }
    }

    private fun fetchSubscription(
        subscriptionUrl: String,
        options: VlessSubscriptionOptions
    ): String {
        val connection = URL(subscriptionUrl).openConnection(Proxy.NO_PROXY) as HttpURLConnection
        connection.connectTimeout = options.probeTimeoutMs
        connection.readTimeout = options.probeTimeoutMs
        connection.requestMethod = "GET"

        return connection.use {
            require(responseCode in 200..299) {
                "Subscription request failed with HTTP $responseCode."
            }
            inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                reader.readText()
            }
        }
    }

    private fun extractVlessLinks(rawBody: String): List<String> {
        val bodies = buildList {
            add(rawBody)
            decodeBase64Variants(rawBody).forEach(::add)
        }

        return bodies
            .asSequence()
            .flatMap { body -> extractVlessLinksFromBody(body).asSequence() }
            .distinct()
            .toList()
    }

    private fun extractVlessLinksFromBody(body: String): List<String> {
        return body
            .lineSequence()
            .flatMap { line -> line.splitToSequence('|') }
            .map { it.trim() }
            .filter { it.startsWith("vless://", ignoreCase = true) }
            .toList()
    }

    private fun decodeBase64Variants(value: String): List<String> {
        val compact = value.filterNot { it.isWhitespace() }
        if (compact.isBlank()) return emptyList()

        val padded = compact.padEnd(compact.length + (4 - compact.length % 4) % 4, '=')
        val inputs = listOf(compact, padded).distinct()
        val flags = listOf(
            Base64.DEFAULT,
            Base64.NO_WRAP,
            Base64.URL_SAFE,
            Base64.URL_SAFE or Base64.NO_WRAP
        )

        return inputs
            .flatMap { input ->
                flags.mapNotNull { flag ->
                    runCatching {
                        String(Base64.decode(input, flag), StandardCharsets.UTF_8)
                    }.getOrNull()
                }
            }
            .filter { it.contains("vless://", ignoreCase = true) }
            .distinct()
            .also { decodedBodies ->
                if (decodedBodies.isEmpty() && compact.contains("vless://", ignoreCase = true).not()) {
                    Log.d(TAG, "Subscription body was not decodable as Base64 with supported variants.")
                }
            }
    }

    private suspend fun measureCandidates(
        links: List<String>,
        options: VlessSubscriptionOptions
    ): List<SelectedVlessServer> = coroutineScope {
        val semaphore = Semaphore(options.maxParallelProbes)

        links.map { link ->
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    measureCandidate(link, options)
                }
            }
        }.awaitAll().filterNotNull()
    }

    private fun measureCandidate(
        link: String,
        options: VlessSubscriptionOptions
    ): SelectedVlessServer? {
        val config = runCatching { VlessUriParser.parse(link) }
            .onFailure { throwable ->
                Log.w(TAG, "Skipping unsupported VLESS link: ${throwable.message}")
            }
            .getOrNull() ?: return null

        val latencyMs = runCatching {
            tcpConnectLatencyMs(config, options.connectTimeoutMs)
        }.onFailure { throwable ->
            Log.d(TAG, "Server ${config.serverAddress}:${config.serverPort} failed probe.", throwable)
        }.getOrNull() ?: return null

        if (latencyMs > options.maxLatencyMs) {
            Log.d(TAG, "Server ${config.serverAddress}:${config.serverPort} latency=${latencyMs}ms rejected.")
            return null
        }

        Log.d(TAG, "Server ${config.serverAddress}:${config.serverPort} latency=${latencyMs}ms accepted.")
        return SelectedVlessServer(
            uri = link,
            config = config,
            latencyMs = latencyMs
        )
    }

    private fun tcpConnectLatencyMs(
        config: VlessProxyConfig,
        connectTimeoutMs: Int
    ): Long {
        val start = SystemClock.elapsedRealtime()
        Socket().use { socket ->
            socket.connect(
                InetSocketAddress(config.serverAddress, config.serverPort),
                connectTimeoutMs
            )
        }
        return SystemClock.elapsedRealtime() - start
    }

    private inline fun <T> HttpURLConnection.use(block: HttpURLConnection.() -> T): T {
        return try {
            block()
        } finally {
            disconnect()
        }
    }

    private companion object {
        private const val TAG = "VlessSubscriptionSelector"
    }
}
