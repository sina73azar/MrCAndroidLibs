package com.mrc.compose_logger.core.formatter

import androidx.core.net.toUri

fun String.toShortUrl(): String {
    return try {
        val uri = this.toUri()
        uri.encodedPath ?: this
    } catch (e: Exception) {
        this
    }
}
