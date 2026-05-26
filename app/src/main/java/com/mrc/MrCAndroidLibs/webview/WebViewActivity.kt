package com.mrc.MrCAndroidLibs.webview

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.http.SslError
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.mrc.MrCAndroidLibs.data.AppNetworkProxyStarter
import com.mrc.networkproxy.core.api.ProxySession
import com.mrc.networkproxy.core.config.LocalProxyType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WebViewActivity : ComponentActivity() {

    @Inject
    lateinit var networkProxyStarter: AppNetworkProxyStarter

    private lateinit var statusText: TextView
    private lateinit var webView: WebView
    private var proxyFallbackStarted = false
    private var proxyOverrideApplied = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusText = TextView(this).apply {
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.DKGRAY)
            text = "Loading facebook.com directly..."
            setPadding(24, 16, 24, 16)
        }

        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = fallbackWebViewClient()
        }

        setContentView(
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                addView(statusText)
                addView(webView)
            }
        )

        clearWebViewProxy {
            webView.loadUrl(FACEBOOK_URL)
        }
    }

    private fun fallbackWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    retryWithProxy()
                }
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                handler.cancel()
                retryWithProxy()
            }
        }
    }

    private fun retryWithProxy() {
        if (proxyFallbackStarted) return
        proxyFallbackStarted = true

        statusText.text = "Direct load failed. Starting proxy..."
        lifecycleScope.launch {
            runCatching {
                val session = networkProxyStarter.ensureStarted()
                applyWebViewProxy(session) {
                    proxyOverrideApplied = true
                    statusText.text = "Retrying facebook.com through proxy..."
                    webView.loadUrl(FACEBOOK_URL)
                }
            }.onFailure { throwable ->
                statusText.text = "Proxy fallback failed: ${throwable.message}"
            }
        }
    }

    private fun applyWebViewProxy(session: ProxySession, onApplied: () -> Unit) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            statusText.text = "WebView proxy override is not supported on this device."
            return
        }

        val proxyUrl = when (session.endpoint.type) {
            LocalProxyType.HTTP -> "http://${session.host}:${session.port}"
            LocalProxyType.SOCKS,
            LocalProxyType.MIXED -> "socks://${session.host}:${session.port}"
        }
        val proxyConfig = ProxyConfig.Builder()
            .addProxyRule(proxyUrl)
            .build()

        ProxyController.getInstance().setProxyOverride(
            proxyConfig,
            ContextCompat.getMainExecutor(this),
            onApplied
        )
    }

    private fun clearWebViewProxy(onCleared: () -> Unit = {}) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            onCleared()
            return
        }

        ProxyController.getInstance().clearProxyOverride(
            ContextCompat.getMainExecutor(this),
            onCleared
        )
    }

    override fun onDestroy() {
        if (proxyOverrideApplied) {
            clearWebViewProxy()
        }
        webView.destroy()
        super.onDestroy()
    }

    private companion object {
        private const val FACEBOOK_URL = "https://www.facebook.com/"
    }
}
