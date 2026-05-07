package com.mrc.compose_logger.core.models

data class NetworkLogEntry(
    val id: String,
    val request: RequestLogModel,
    val response: ResponseLogModel? = null,
    val error: String? = null,
    val durationMs: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class RequestLogModel(
    val method: String,
    val url: String,
    val headers: List<HeaderModel>,
    val body: String?
)

data class ResponseLogModel(
    val code: Int,
    val message: String,
    val headers: List<HeaderModel>,
    val body: String?
)

data class HeaderModel(
    val name: String,
    val value: String
)