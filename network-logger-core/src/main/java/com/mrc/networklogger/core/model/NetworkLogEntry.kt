package com.mrc.networklogger.core.model

data class NetworkLogEntry(
    val id: String,
    val request: RequestLogModel,
    val response: ResponseLogModel? = null,
    val error: String? = null,
    val durationMs: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)