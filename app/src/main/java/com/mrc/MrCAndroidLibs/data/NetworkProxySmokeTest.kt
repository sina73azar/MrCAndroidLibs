package com.mrc.MrCAndroidLibs.data

import android.content.Context
import android.util.Log
import com.mrc.networkproxy.core.api.NetworkProxy
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

    fun startExpectedMissingEngineTest() {
        scope.launch {
            val result = runCatching {
                NetworkProxy.start(
                    context = context,
                    vlessUri = TEST_VLESS_URI
                )
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
            "vless://00000000-0000-4000-8000-000000000000@example.com:443?encryption=none&security=tls&sni=example.com#SmokeTest"
    }
}
