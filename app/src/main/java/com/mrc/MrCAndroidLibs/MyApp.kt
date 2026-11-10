package com.mrc.MrCAndroidLibs

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroidOptions
import ir.irananalytics.android.IranAnalytics

/**
 * Initializes Iran Analytics (Sentry-compatible) for the team clone ingest at 62.220.122.12:9000,
 * Sentry project **android** (id 9). Replace the public key from Settings → Client Keys if ingest fails.
 */
@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        IranAnalytics.init(this) { options: SentryAndroidOptions ->
            options.dsn = SENTRY_CLONE_DSN
            options.environment = "debug"
            options.release =
                "${BuildConfig.APPLICATION_ID}@${BuildConfig.VERSION_NAME}+${BuildConfig.VERSION_CODE}"
            options.isDebug = true
            options.tracesSampleRate = 1.0
        }
    }

    companion object {
        /** Pattern: `http://<PUBLIC_KEY>@62.220.122.12:9000/<PROJECT_ID>` */
        private const val SENTRY_CLONE_DSN =
            "http://f6d0bcb627a3ea2ad6af3380400408e4@62.220.122.12:9000/9"
    }
}
