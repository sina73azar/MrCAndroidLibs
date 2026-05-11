package com.mrc.networklogger.core.internal

import com.mrc.networklogger.core.interceptor.LoggerInterceptor
import com.mrc.networklogger.core.store.LoggerStore

/**
 * Mr.C 07/May/2026
 */
object ServiceLocator {

    private val loggerStore: LoggerStore by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LoggerStore()
    }

    private val loggerInterceptor: LoggerInterceptor by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LoggerInterceptor(loggerStore)
    }

    fun interceptor(): LoggerInterceptor = loggerInterceptor

    fun store(): LoggerStore = loggerStore
}