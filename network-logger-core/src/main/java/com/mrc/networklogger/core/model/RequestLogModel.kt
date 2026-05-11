package com.mrc.networklogger.core.model

/**
 * Mr.C 10/May/2026
 */
data class RequestLogModel(
    val method: String,
    val url: String,
    val headers: List<HeaderModel>,
    val body: String?
)