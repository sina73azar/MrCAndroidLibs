package com.mrc.networkproxy.core.api

import android.content.Context
import com.mrc.networkproxy.core.config.LocalProxyEndpoint
import com.mrc.networkproxy.core.config.NetworkProxyOptions
import com.mrc.networkproxy.core.config.ProxyStartRequest
import com.mrc.networkproxy.core.config.SingBoxConfigBuilder
import com.mrc.networkproxy.core.config.VlessProxyConfig
import com.mrc.networkproxy.core.config.VlessUriParser
import com.mrc.networkproxy.core.config.toJavaProxyType
import com.mrc.networkproxy.core.engine.EngineUnavailableException
import com.mrc.networkproxy.core.engine.ProxyEngine
import com.mrc.networkproxy.core.engine.RunningProxyEngine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.InetSocketAddress
import java.net.Proxy

object NetworkProxy {

    private val mutex = Mutex()
    private var activeSession: ProxySession? = null

    val currentSession: ProxySession?
        get() = activeSession

    suspend fun start(
        context: Context,
        vlessUri: String,
        options: NetworkProxyOptions = NetworkProxyOptions(),
        engine: ProxyEngine = MissingProxyEngine
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
        engine: ProxyEngine = MissingProxyEngine
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

    suspend fun stop() {
        mutex.withLock {
            activeSession?.engine?.stop()
            activeSession = null
        }
    }

    private object MissingProxyEngine : ProxyEngine {
        override suspend fun start(request: ProxyStartRequest): RunningProxyEngine {
            throw EngineUnavailableException(
                "No proxy engine was provided. Wire a sing-box/Xray/Rust ProxyEngine before starting the tunnel."
            )
        }
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
