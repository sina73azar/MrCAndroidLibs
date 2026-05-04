package com.mrc.MrCAndroidLibs.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mrc.MrCAndroidLibs.data.ApiService
import com.mrc.MrCAndroidLibs.data.AppRepo
import com.mrc.MrCAndroidLibs.data.AppRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Mr.C 04/May/2026
 */
@Module
@InstallIn(value = [ActivityRetainedComponent::class])
object AppModule {

    const val BASE_URL = "https://jsonplaceholder.typicode.com"

    @Provides
    fun buildOkhttp(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .addNetworkInterceptor(
                HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT).apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()
    }


    @Provides
    fun buildApiService(okHttpClient: OkHttpClient): ApiService {
        val json = Json { ignoreUnknownKeys = true }

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    fun buildRepo(appRepoImpl: AppRepoImpl): AppRepo = appRepoImpl
}

