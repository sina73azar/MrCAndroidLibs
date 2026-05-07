package com.mrc.compose_logger.core.filters

import com.mrc.compose_logger.core.filters.StatusFilter

data class LogFilterState(
    val query: String = "",
    val method: String? = null,
    val status: StatusFilter = StatusFilter.ALL
)