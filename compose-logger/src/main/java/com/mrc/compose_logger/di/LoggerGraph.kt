package com.mrc.compose_logger.di

import com.mrc.compose_logger.data.LoggerInterceptor
import com.mrc.compose_logger.data.LoggerStore

/**
 * Mr.C 07/May/2026
 */
internal object LoggerGraph {

    val loggerStore: LoggerStore by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LoggerStore()
    }

    val loggerInterceptor: LoggerInterceptor by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LoggerInterceptor(loggerStore)
    }
}