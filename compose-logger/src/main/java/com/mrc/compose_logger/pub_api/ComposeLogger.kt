package com.mrc.compose_logger.pub_api

import android.content.Context
import android.content.Intent
import com.mrc.compose_logger.data.LoggerInterceptor
import com.mrc.compose_logger.data.LoggerStore
import com.mrc.compose_logger.di.LoggerGraph
import com.mrc.compose_logger.presentation.LoggerActivity
import okhttp3.Interceptor

object ComposeLogger {

    fun interceptor(): Interceptor {
        return LoggerGraph.loggerInterceptor
    }

    fun launch(context: Context) {
        context.startActivity(
            Intent(context, LoggerActivity::class.java)
        )
    }

    fun clearLogs() {
        LoggerGraph.loggerStore.clear()
    }
}