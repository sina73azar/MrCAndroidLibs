package com.mrc.compose_logger.api

import android.content.Context
import android.content.Intent
import com.mrc.compose_logger.presentation.activity.LoggerActivity

object LoggerLauncher {

    fun open(context: Context) {
        context.startActivity(
            Intent(context, LoggerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}