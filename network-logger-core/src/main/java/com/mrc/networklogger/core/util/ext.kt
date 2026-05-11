package com.mrc.networklogger.core.util

import androidx.core.net.toUri

fun String.toShortUrl(): String {
    return try {
        val uri = this.toUri()
        uri.encodedPath ?: this
    } catch (e: Exception) {
        this
    }
}
