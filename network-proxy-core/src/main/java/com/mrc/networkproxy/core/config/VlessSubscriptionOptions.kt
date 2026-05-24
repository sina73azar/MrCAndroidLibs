package com.mrc.networkproxy.core.config

data class VlessSubscriptionOptions(
    val maxLatencyMs: Int = 1500,
    val connectTimeoutMs: Int = 2000,
    val maxParallelProbes: Int = 8,
    val probeUrl: String = "https://dummyjson.com/posts",
    val probeTimeoutMs: Int = 10_000
)

data class SelectedVlessServer(
    val uri: String,
    val config: VlessProxyConfig,
    val latencyMs: Long
)
