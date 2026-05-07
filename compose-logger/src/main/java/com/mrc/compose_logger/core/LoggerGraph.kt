package com.mrc.compose_logger.core

import com.mrc.compose_logger.api.interceptor.LoggerInterceptor

/**
 * Mr.C 07/May/2026
 */
internal object LoggerGraph {

    private val loggerStore: LoggerStore by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LoggerStore()
    }

    private val loggerInterceptor: LoggerInterceptor by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LoggerInterceptor(loggerStore)
    }

    fun interceptor(): LoggerInterceptor = loggerInterceptor

    fun store(): LoggerStore = loggerStore
}