package com.mrc.MrCAndroidLibs.data

import android.content.Context
import android.util.Log
import com.mrc.MrCAndroidLibs.BuildConfig
import com.mrc.networkproxy.core.api.NetworkProxy
import com.mrc.networkproxy.core.api.ProxySession
import com.mrc.networkproxy.core.config.NetworkProxyOptions
import com.mrc.networkproxy.core.config.VlessSubscriptionOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNetworkProxyStarter @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    suspend fun ensureStarted(): ProxySession {
        NetworkProxy.currentSession?.let { return it }

        return NetworkProxy.startFromSubscription(
            context = context,
            subscriptionUrl = BuildConfig.VLESS_SUBSCRIPTION_URL,
            options = NetworkProxyOptions(logLevel = "debug"),
            subscriptionOptions = VlessSubscriptionOptions(
                maxLatencyMs = BuildConfig.VLESS_ACCEPTABLE_LATENCY_MS
            )
        ).also { session ->
            Log.d(TAG, "Proxy engine started at ${session.host}:${session.port}.")
        }
    }

    private companion object {
        private const val TAG = "AppNetworkProxyStarter"
    }
}
