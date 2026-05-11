package com.mrc.networklogger.core.model

/**
 * Mr.C 10/May/2026
 */
data class ResponseLogModel(
    val code: Int,
    val message: String,
    val headers: List<HeaderModel>,
    val body: String?
)