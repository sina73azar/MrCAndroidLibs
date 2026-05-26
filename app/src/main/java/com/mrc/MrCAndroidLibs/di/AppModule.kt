package com.mrc.MrCAndroidLibs.di

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mrc.MrCAndroidLibs.data.ApiService
import com.mrc.MrCAndroidLibs.data.AppRepo
import com.mrc.MrCAndroidLibs.data.AppRepoImpl
import com.mrc.networklogger.core.api.NetworkLogger
import com.mrc.networkproxy.core.api.NetworkProxy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI
import javax.inject.Singleton

/**
 * Mr.C 04/May/2026
 */
@Module
@InstallIn(value = [SingletonComponent::class])
object AppModule {


    @Provides
    @Singleton
    fun buildOkhttp(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .proxySelector(activeProxySelector())
            .eventListenerFactory(tunnelLoggingEventListenerFactory())
            .addNetworkInterceptor(NetworkLogger.interceptor)
            .build()
    }


    @Provides
    @Singleton
    fun buildApiService(okHttpClient: OkHttpClient): ApiService {
        val json = Json { ignoreUnknownKeys = true }

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://dummyjson.com/")
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun buildRepo(appRepoImpl: AppRepoImpl): AppRepo = appRepoImpl

    private fun activeProxySelector(): ProxySelector {
        return object : ProxySelector() {
            override fun select(uri: URI?): List<Proxy> {
                val proxy = NetworkProxy.currentSession?.proxy ?: Proxy.NO_PROXY
                Log.d(TAG, "ProxySelector.select uri=$uri proxy=$proxy")
                return listOf(proxy)
            }

            override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
                Log.e(TAG, "ProxySelector.connectFailed uri=$uri socketAddress=$sa", ioe)
            }
        }
    }

    private fun tunnelLoggingEventListenerFactory(): EventListener.Factory {
        return EventListener.Factory {
            object : EventListener() {
                override fun connectStart(
                    call: Call,
                    inetSocketAddress: InetSocketAddress,
                    proxy: Proxy
                ) {
                    Log.d(
                        TAG,
                        "OkHttp connectStart url=${call.request().url} target=$inetSocketAddress proxy=$proxy"
                    )
                }
            }
        }
    }

    private const val TAG = "NetworkProxyTunnel"
}
