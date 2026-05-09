package com.mrc.compose_logger.api

import com.mrc.compose_logger.core.LoggerGraph
import okhttp3.Interceptor

/**
 * Mr.C 5/9/26
 * only api for network data wiring that lib user can access
 */
object ComposeLogger {

    val interceptor: Interceptor
        get() = LoggerGraph.interceptor()
}