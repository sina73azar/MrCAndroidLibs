package com.mrc.MrCAndroidLibs.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mrc.MrCAndroidLibs.data.ApiService
import com.mrc.MrCAndroidLibs.data.AppRepo
import com.mrc.MrCAndroidLibs.data.AppRepoImpl
import com.mrc.networklogger.core.api.NetworkLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Mr.C 04/May/2026
 */
@Module
@InstallIn(value = [ActivityRetainedComponent::class])
object AppModule {


    @Provides
    fun buildOkhttp(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .addNetworkInterceptor(NetworkLogger.interceptor)
            .build()
    }


    @Provides
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
    fun buildRepo(appRepoImpl: AppRepoImpl): AppRepo = appRepoImpl
}

