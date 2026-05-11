package com.mrc.networklogger.ui.utils

import androidx.compose.ui.graphics.Color

object StatusColor {

    fun fromCode(code: Int?): Color {
        return when (code) {
            null -> Color(0xFFD32F2F) // error / no response
            in 200..299 -> Color(0xFF388E3C) // green
            in 300..399 -> Color(0xFF1976D2) // blue
            in 400..499 -> Color(0xFFF57C00) // orange
            in 500..599 -> Color(0xFFD32F2F) // red
            else -> Color.Gray
        }
    }
}