package com.mrc.networklogger.core.api

import com.mrc.networklogger.core.interceptor.LoggerInterceptor
import com.mrc.networklogger.core.internal.ServiceLocator

/**
 * Mr.C 5/9/26
 * only api for network data wiring that lib user can access
 */
object NetworkLogger {

    val interceptor: LoggerInterceptor by lazy {
        ServiceLocator.interceptor()
    }
}