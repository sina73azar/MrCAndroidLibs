package com.mrc.networkproxy.core.api

import android.content.Context
import android.util.Log
import com.mrc.networkproxy.core.config.LocalProxyEndpoint
import com.mrc.networkproxy.core.config.NetworkProxyOptions
import com.mrc.networkproxy.core.config.ProxyStartRequest
import com.mrc.networkproxy.core.config.SingBoxConfigBuilder
import com.mrc.networkproxy.core.config.VlessProxyConfig
import com.mrc.networkproxy.core.config.VlessSubscriptionOptions
import com.mrc.networkproxy.core.config.VlessUriParser
import com.mrc.networkproxy.core.config.toJavaProxyType
import com.mrc.networkproxy.core.engine.LibboxProxyEngine
import com.mrc.networkproxy.core.engine.ProxyEngine
import com.mrc.networkproxy.core.engine.RunningProxyEngine
import com.mrc.networkproxy.core.subscription.VlessSubscriptionSelector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

object NetworkProxy {

    private val mutex = Mutex()
    private var activeSession: ProxySession? = null

    val currentSession: ProxySession?
        get() = activeSession

    suspend fun start(
        context: Context,
        vlessUri: String,
        options: NetworkProxyOptions = NetworkProxyOptions(),
        engine: ProxyEngine = LibboxProxyEngine()
    ): ProxySession {
        return start(
            context = context,
            config = VlessUriParser.parse(vlessUri),
            options = options,
            engine = engine
        )
    }

    suspend fun start(
        context: Context,
        config: VlessProxyConfig,
        options: NetworkProxyOptions = NetworkProxyOptions(),
        engine: ProxyEngine = LibboxProxyEngine()
    ): ProxySession = mutex.withLock {
        activeSession?.let { return@withLock it }

        val endpoint = LocalProxyEndpoint(
            host = options.localHost,
            port = options.localPort,
            type = options.localProxyType
        )
        val singBoxConfig = SingBoxConfigBuilder.build(
            vless = config,
            endpoint = endpoint,
            options = options
        )
        Log.d(TAG, "Starting proxy endpoint=$endpoint vless=$config")
        Log.d(TAG, "Generated sing-box config: $singBoxConfig")
        val runningEngine = engine.start(
            ProxyStartRequest(
                applicationContext = context.applicationContext,
                endpoint = endpoint,
                singBoxConfigJson = singBoxConfig
            )
        )

        ProxySession(
            endpoint = runningEngine.endpoint,
            engine = runningEngine
        ).also { activeSession = it }
    }

    suspend fun startFromSubscription(
        context: Context,
        subscriptionUrl: String,
        options: NetworkProxyOptions = NetworkProxyOptions(logLevel = "debug"),
        subscriptionOptions: VlessSubscriptionOptions = VlessSubscriptionOptions(),
        selector: VlessSubscriptionSelector = VlessSubscriptionSelector(),
        engineFactory: () -> ProxyEngine = { LibboxProxyEngine() }
    ): ProxySession = withContext(Dispatchers.IO) {
        currentSession?.let { return@withContext it }

        val candidates = selector.selectCandidates(
            subscriptionUrl = subscriptionUrl,
            options = subscriptionOptions
        )
        var lastFailure: Throwable? = null

        candidates.forEach { selectedServer ->
            Log.d(
                TAG,
                "Trying VLESS server ${selectedServer.config.serverAddress}:" +
                    "${selectedServer.config.serverPort} latency=${selectedServer.latencyMs}ms"
            )
            Log.d(TAG, "Trying VLESS config: ${selectedServer.config}")
            Log.d(TAG, "Trying VLESS uri: ${selectedServer.uri}")

            val session = runCatching {
                start(
                    context = context,
                    vlessUri = selectedServer.uri,
                    options = options,
                    engine = engineFactory()
                )
            }.onFailure { throwable ->
                lastFailure = throwable
                Log.w(TAG, "Proxy engine failed for ${selectedServer.config.serverAddress}.", throwable)
                stop()
            }.getOrNull() ?: return@forEach

            val tunnelWorks = runCatching {
                probeTunnel(session, subscriptionOptions)
            }.onFailure { throwable ->
                lastFailure = throwable
                Log.w(TAG, "Tunnel probe failed for ${selectedServer.config.serverAddress}.", throwable)
            }.getOrDefault(false)

            if (tunnelWorks) {
                Log.d(
                    TAG,
                    "Selected VLESS server ${selectedServer.config.serverAddress}:" +
                        "${selectedServer.config.serverPort} latency=${selectedServer.latencyMs}ms"
                )
                Log.d(TAG, "Selected VLESS config: ${selectedServer.config}")
                Log.d(TAG, "Selected VLESS uri: ${selectedServer.uri}")
                return@withContext session
            }

            stop()
        }

        throw IllegalStateException("No probed VLESS tunnel completed an HTTP request.", lastFailure)
    }

    suspend fun stop() {
        mutex.withLock {
            activeSession?.engine?.stop()
            activeSession = null
        }
    }

}

private const val TAG = "NetworkProxy"

private fun probeTunnel(
    session: ProxySession,
    options: VlessSubscriptionOptions
): Boolean {
    val connection = URL(options.probeUrl).openConnection(session.proxy) as HttpURLConnection
    connection.connectTimeout = options.probeTimeoutMs
    connection.readTimeout = options.probeTimeoutMs
    connection.requestMethod = "GET"

    return connection.use {
        Log.d(TAG, "Tunnel probe response code=$responseCode url=${options.probeUrl}")
        responseCode in 200..299
    }
}

private inline fun <T> HttpURLConnection.use(block: HttpURLConnection.() -> T): T {
    return try {
        block()
    } finally {
        disconnect()
    }
}

class ProxySession internal constructor(
    val endpoint: LocalProxyEndpoint,
    internal val engine: RunningProxyEngine
) {
    val proxy: Proxy
        get() = Proxy(
            endpoint.type.toJavaProxyType(),
            InetSocketAddress(endpoint.host, endpoint.port)
        )

    val host: String
        get() = endpoint.host

    val port: Int
        get() = endpoint.port
}
