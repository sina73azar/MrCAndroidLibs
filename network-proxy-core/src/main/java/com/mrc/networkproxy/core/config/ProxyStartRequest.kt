package com.mrc.networkproxy.core.config

import android.content.Context

data class ProxyStartRequest(
    val applicationContext: Context,
    val endpoint: LocalProxyEndpoint,
    val singBoxConfigJson: String
)
