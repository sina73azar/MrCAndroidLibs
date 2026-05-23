package com.mrc.MrCAndroidLibs.data

import android.content.Context
import android.util.Log
import com.mrc.networkproxy.core.api.NetworkProxy
import com.mrc.networkproxy.core.api.ProxySession
import com.mrc.networkproxy.core.config.NetworkProxyOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkProxySmokeTest @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun ensureStarted(): ProxySession {
        NetworkProxy.currentSession?.let { return it }
        return NetworkProxy.start(
            context = context,
            vlessUri = TEST_VLESS_URI,
            options = NetworkProxyOptions(logLevel = "debug")
        ).also { session ->
            Log.d(TAG, "Proxy engine started at ${session.host}:${session.port}.")
        }
    }

    fun startExpectedMissingEngineTest() {
        scope.launch {
            val result = runCatching {
                ensureStarted()
            }

            result.exceptionOrNull()?.let { throwable ->
                Log.e(TAG, "Proxy engine smoke test failed.", throwable)
            } ?: NetworkProxy.currentSession?.let { session ->
                Log.d(TAG, "Proxy engine started at ${session.host}:${session.port}.")
            }
        }
    }

    private companion object {
        private const val TAG = "NetworkProxySmokeTest"
        private const val TEST_VLESS_URI =
            "vless://c0565421-e8e8-47d5-b21c-926cf80179ff@162.159.36.5:443?encryption=none&security=tls&type=xhttp&path=%2Fsibzaminiz&host=gxr1.parhamm1.ir&sni=gxr1.parhamm1.ir&fp=chrome&allowInsecure=true&mode=auto#%D8%A2%D9%84%D9%85%D8%A7%D9%86%20%D8%AC%D8%A7%DB%8C%DA%AF%D8%B2%DB%8C%D9%86%201%20%F0%9F%87%A9%F0%9F%87%AA"
    }
}
