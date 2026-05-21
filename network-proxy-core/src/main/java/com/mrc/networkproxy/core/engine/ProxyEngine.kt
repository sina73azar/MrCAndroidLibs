package com.mrc.networkproxy.core.engine

import com.mrc.networkproxy.core.config.LocalProxyEndpoint
import com.mrc.networkproxy.core.config.ProxyStartRequest

interface ProxyEngine {
    suspend fun start(request: ProxyStartRequest): RunningProxyEngine
}

interface RunningProxyEngine {
    val endpoint: LocalProxyEndpoint

    suspend fun stop()
}

class EngineUnavailableException(message: String) : IllegalStateException(message)
