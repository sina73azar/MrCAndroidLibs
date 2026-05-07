package com.mrc.compose_logger.presentation.compose_ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.mrc.compose_logger.R
import com.mrc.compose_logger.presentation.LoggerActivity

/**
 * Mr.C 13/Apr/2026
 */
@Composable
fun DebugFloatingButton() {
//    if (!BuildConfig.DEBUG) return

    val context = LocalContext.current
    if (context is LoggerActivity) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = {
                context.startActivity(Intent(context, LoggerActivity::class.java))
            },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_circle_info),
                contentDescription = "Debug Logs"
            )
        }
    }
}