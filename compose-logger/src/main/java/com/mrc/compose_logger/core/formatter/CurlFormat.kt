package com.mrc.compose_logger.core.formatter

import com.mrc.compose_logger.core.models.NetworkLogEntry

fun NetworkLogEntry.toCurl(): String = buildString {
    append("curl -X ${request.method} ")

    request.headers.forEach {
        append("-H \"${it.name}: ${it.value}\" ")
    }

    request.body?.let {
        append("-d '${it.replace("'", "\\'")}' ")
    }

    append("\"${request.url}\"")
}
