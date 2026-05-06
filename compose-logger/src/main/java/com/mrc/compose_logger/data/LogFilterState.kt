package com.mrc.compose_logger.data

data class LogFilterState(
    val query: String = "",
    val method: String? = null,
    val status: StatusFilter = StatusFilter.ALL
)
