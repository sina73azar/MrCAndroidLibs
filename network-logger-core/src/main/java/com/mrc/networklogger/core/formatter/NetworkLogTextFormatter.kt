package com.mrc.networklogger.core.formatter


import com.mrc.networklogger.core.model.NetworkLogEntry

object NetworkLogTextFormatter {

    fun format(entry: NetworkLogEntry): String = buildString {

        appendLine("========== NETWORK CALL ==========")
        appendLine()

        // Request
        appendLine("▶ REQUEST")
        appendLine("${entry.request.method} ${entry.request.url}")

        if (entry.request.headers.isNotEmpty()) {
            appendLine("Headers:")
            entry.request.headers.forEach {
                appendLine("  ${it.name}: ${it.value}")
            }
        }

        entry.request.body?.takeIf { it.isNotBlank() }?.let {
            appendLine()
            appendLine("Body:")
            appendLine(it)
        }

        appendLine()
        appendLine("----------------------------------")

        // Response
        entry.response?.let { response ->
            appendLine("◀ RESPONSE")
            appendLine("Code: ${response.code}")
            appendLine("Duration: ${entry.durationMs} ms")

            if (response.headers.isNotEmpty()) {
                appendLine("Headers:")
                response.headers.forEach {
                    appendLine("  ${it.name}: ${it.value}")
                }
            }

            response.body?.takeIf { it.isNotBlank() }?.let {
                appendLine()
                appendLine("Body:")
                appendLine(it)
            }
        } ?: run {
            appendLine("◀ RESPONSE")
            appendLine("ERROR")
            appendLine(entry.error ?: "Unknown error")
        }

        appendLine()
        appendLine("==================================")
    }
}