package com.mrc.networklogger.core.filter

data class LogFilterState(
    val query: String = "",
    val method: String? = null,
    val status: StatusFilter = StatusFilter.ALL
)