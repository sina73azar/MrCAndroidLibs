package com.mrc.compose_logger.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.mrc.compose_logger.presentation.screen.LoggerScreen
import com.mrc.compose_logger.ui.theme.MrCAndroidLibsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoggerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MrCAndroidLibsTheme {
                /**
                 * because its going to be english logs so we go against direction of whole theme here
                 * */
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Ltr,
                ) {
                    LoggerScreen()
                }
            }
        }
    }
}