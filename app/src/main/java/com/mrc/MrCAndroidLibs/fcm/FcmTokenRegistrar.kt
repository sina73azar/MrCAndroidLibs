package com.mrc.MrCAndroidLibs.fcm

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.mrc.MrCAndroidLibs.BuildConfig
import com.mrc.MrCAndroidLibs.data.ApiService
import com.mrc.MrCAndroidLibs.data.AppNetworkProxyStarter
import com.mrc.MrCAndroidLibs.data.FcmRegistrationRequest
import com.mrc.MrCAndroidLibs.data.ScopedGlobalProxy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FcmTokenRegistrar @Inject constructor(
    private val apiService: ApiService,
    private val networkProxyStarter: AppNetworkProxyStarter
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun registerCurrentToken() {
        scope.launch {
            runCatching {
                val token = fetchTokenWithProxyFallback()
                registerToken(token)
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                Log.e(TAG, "Failed to register current FCM token.", throwable)
            }
        }
    }

    fun registerTokenAsync(token: String) {
        scope.launch {
            runCatching {
                registerToken(token)
            }.onFailure { throwable ->
                if (throwable is CancellationException) throw throwable
                Log.e(TAG, "Failed to register refreshed FCM token.", throwable)
            }
        }
    }

    private suspend fun registerToken(token: String) {
        val registrationUrl = BuildConfig.FCM_REGISTRATION_URL
        if (registrationUrl.isBlank()) {
            Log.d(TAG, "FCM token received but FCM_REGISTRATION_URL is empty. token=$token")
            return
        }

        val request = FcmRegistrationRequest(token = token)
        val response = runCatching {
            apiService.registerFcmToken(
                url = registrationUrl,
                request = request
            )
        }.getOrElse { throwable ->
            if (throwable is CancellationException) throw throwable
            Log.w(TAG, "Direct backend FCM registration failed. Retrying through proxy.", throwable)
            val session = networkProxyStarter.ensureStarted()
            ScopedGlobalProxy.install(session).use {
                apiService.registerFcmToken(
                    url = registrationUrl,
                    request = request
                )
            }
        }

        if (response.isSuccessful) {
            Log.d(TAG, "FCM token registered with backend. code=${response.code()}")
        } else {
            Log.e(
                TAG,
                "Backend FCM registration failed. code=${response.code()} message=${response.message()}"
            )
        }
    }

    private suspend fun fetchTokenWithProxyFallback(): String {
        return runCatching {
            Log.d(TAG, "Fetching FCM token without app proxy.")
            FirebaseMessaging.getInstance().token.await()
        }.getOrElse { throwable ->
            if (throwable is CancellationException) throw throwable
            Log.w(TAG, "Direct FCM token fetch failed. Retrying through proxy.", throwable)
            val session = networkProxyStarter.ensureStarted()
            ScopedGlobalProxy.install(session).use {
                FirebaseMessaging.getInstance().token.await()
            }
        }
    }

    fun close() {
        scope.cancel()
    }

    private companion object {
        private const val TAG = "FcmTokenRegistrar"
    }
}

private suspend fun <T> Task<T>.await(): T {
    if (isComplete) {
        val exception = exception
        if (exception != null) throw exception
        if (isCanceled) throw CancellationException("Task was cancelled.")
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            val exception = task.exception
            when {
                exception != null -> continuation.resumeWithException(exception)
                task.isCanceled -> continuation.cancel(CancellationException("Task was cancelled."))
                else -> continuation.resume(task.result)
            }
        }
    }
}
