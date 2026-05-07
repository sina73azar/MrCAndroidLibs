package com.mrc.compose_logger.utils

import android.net.Uri
import androidx.core.net.toUri

fun String.toShortUrl(): String {
    return try {
        val uri = this.toUri()
        uri.encodedPath ?: this
    } catch (e: Exception) {
        this
    }
}
