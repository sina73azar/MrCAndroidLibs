package com.mrc.compose_logger.api

import android.content.Context
import android.content.Intent
import com.mrc.compose_logger.core.LoggerGraph
import com.mrc.compose_logger.presentation.activity.LoggerActivity
import okhttp3.Interceptor

object ComposeLogger {


    val interceptor: Interceptor
        get() = LoggerGraph.interceptor()
}