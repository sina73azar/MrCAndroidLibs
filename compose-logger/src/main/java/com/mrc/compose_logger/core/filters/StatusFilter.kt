package com.mrc.compose_logger.core.filters

enum class StatusFilter(val displayName:String) {
    ALL("ALL"),
    SUCCESS_2XX("2XX"),
    CLIENT_4XX("4XX"),
    SERVER_5XX("5XX"),
    ERROR("ERROR")
}