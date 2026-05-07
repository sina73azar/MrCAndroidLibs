package com.mrc.compose_logger.api.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.mrc.compose_logger.R
import com.mrc.compose_logger.api.LoggerLauncher
import com.mrc.compose_logger.presentation.activity.LoggerActivity

/**
 * Mr.C 13/Apr/2026
 */
@Composable
fun LoggerFab(
    modifier: Modifier = Modifier,
    visible: Boolean = /*BuildConfig.DEBUG*/true,
    alignment: Alignment = Alignment.BottomEnd,
    onClick: (() -> Unit)? = null
) {
    if (!visible) return

    val context = LocalContext.current

    if (context is LoggerActivity) return

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        FloatingActionButton(
            modifier = modifier.padding(24.dp),
            onClick = {
                onClick?.invoke()
                    ?: LoggerLauncher.open(context)
            }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_circle_info),
                contentDescription = null
            )
        }
    }
}