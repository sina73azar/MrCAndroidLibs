package com.mrc.MrCAndroidLibs.data

import android.util.Log
import com.mrc.networkproxy.core.api.ProxySession
import com.mrc.networkproxy.core.config.LocalProxyType
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

class ScopedGlobalProxy private constructor(
    private val previousSelector: ProxySelector?,
    private val previousProperties: Map<String, String?>
) : AutoCloseable {

    override fun close() {
        ProxySelector.setDefault(previousSelector)
        previousProperties.forEach { (key, value) ->
            if (value == null) {
                System.clearProperty(key)
            } else {
                System.setProperty(key, value)
            }
        }
    }

    companion object {
        private const val TAG = "ScopedGlobalProxy"

        private val PROXY_PROPERTY_KEYS = listOf(
            "http.proxyHost",
            "http.proxyPort",
            "https.proxyHost",
            "https.proxyPort",
            "socksProxyHost",
            "socksProxyPort"
        )

        fun install(session: ProxySession): ScopedGlobalProxy {
            val scope = ScopedGlobalProxy(
                previousSelector = ProxySelector.getDefault(),
                previousProperties = PROXY_PROPERTY_KEYS.associateWith { key ->
                    System.getProperty(key)
                }
            )

            ProxySelector.setDefault(object : ProxySelector() {
                override fun select(uri: URI?): List<Proxy> {
                    return listOf(session.proxy)
                }

                override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: java.io.IOException?) {
                    Log.e(TAG, "Temporary global proxy connectFailed uri=$uri socketAddress=$sa", ioe)
                }
            })

            clearProxyProperties()
            if (session.endpoint.type == LocalProxyType.HTTP) {
                System.setProperty("http.proxyHost", session.host)
                System.setProperty("http.proxyPort", session.port.toString())
                System.setProperty("https.proxyHost", session.host)
                System.setProperty("https.proxyPort", session.port.toString())
            } else {
                System.setProperty("socksProxyHost", session.host)
                System.setProperty("socksProxyPort", session.port.toString())
            }

            return scope
        }

        private fun clearProxyProperties() {
            PROXY_PROPERTY_KEYS.forEach(System::clearProperty)
        }
    }
}
