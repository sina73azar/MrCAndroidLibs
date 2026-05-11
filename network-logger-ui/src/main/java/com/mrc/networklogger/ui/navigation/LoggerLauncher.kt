package com.mrc.networklogger.ui.navigation

import android.content.Context
import android.content.Intent
import com.mrc.networklogger.ui.activity.LoggerActivity

object LoggerLauncher {

    fun open(context: Context) {
        context.startActivity(
            Intent(context, LoggerActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}