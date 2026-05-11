package com.mrc.networklogger.core.formatter

import com.mrc.networklogger.core.model.NetworkLogEntry

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
