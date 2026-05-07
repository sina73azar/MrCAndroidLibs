package com.mrc.compose_logger.presentation.compose_ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrc.compose_logger.data.HeaderModel
import com.mrc.compose_logger.data.NetworkLogEntry
import com.mrc.compose_logger.utils.JsonUtils
import com.mrc.compose_logger.utils.NetworkLogTextFormatter
import com.mrc.compose_logger.utils.StatusColor
import com.mrc.compose_logger.utils.toCurl
import com.mrc.compose_logger.utils.toShortUrl

@Composable
fun NetworkLogItem(
    log: NetworkLogEntry
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val statusCode = log.response?.code
    val statusColor = StatusColor.fromCode(statusCode)



    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
            ),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
        ) {

            MethodChip(
                method = log.request.method,
                modifier = Modifier.padding(end = 8.dp)
            )

            Text(
                text = log.request.url.toShortUrl(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.width(8.dp))

            when {
                statusCode != null -> {
                    Text(
                        text = statusCode.toString(),
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                log.error != null -> {
                    Text(
                        text = "ERROR",
                        color = StatusColor.fromCode(null),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

//            Spacer(Modifier.width(8.dp))


            // ✅ Action menu
            LogItemActionMenu(
                onCopy = {
                    val text = NetworkLogTextFormatter.format(log)
                    context.copyToClipboard(
                        label = "Network log",
                        text = text,
                        toastMessage = "Network log copied"
                    )
                },
                onCopyCurl = {
                    context.copyToClipboard(
                        label = "Curl",
                        text = log.toCurl(),
                        toastMessage = "Curl copied"
                    )
                }
            )
        }

        // Details—now correctly attached to the bottom of the Card
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            NetworkLogDetails(log)
        }

    }
}


@Composable
fun NetworkLogDetails(
    log: NetworkLogEntry
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)  // no top padding
    ) {

        Text(
            "Request",
            style = MaterialTheme.typography.labelLarge
        )

        KeyValueSection("Method", log.request.method)
        KeyValueSection("URL", log.request.url)

        HeadersSection(log.request.headers)

        log.request.body?.let {
            BodySection("Request Body", it)
        }

        SpacerSmall()

        log.response?.let { response ->
            Text(
                "Response",
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = "Code: ${response.code}",
                color = StatusColor.fromCode(response.code),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
            KeyValueSection("Message", response.message)

            HeadersSection(response.headers)

            response.body?.let {
                BodySection("Response Body", it)
            }
        }

        log.error?.let {
            BodySection("Error", it)
        }
    }
}

@Composable
private fun HeadersSection(headers: List<HeaderModel>) {
    if (headers.isEmpty()) return

    Text("Headers", style = MaterialTheme.typography.labelMedium)

    headers.forEach {
        KeyValueSection(it.name, it.value)
    }
}

@Composable
private fun KeyValueSection(key: String, value: String) {
    Text(
        text = "$key: $value",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun BodySection(
    title: String,
    body: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Collapse" else "Expand")
            }
        }

        val annotated = remember(body) {
            JsonUtils.prettify(body)
        }

        Text(
            text = annotated,
            style = MaterialTheme.typography.bodySmall,
            maxLines = if (expanded) Int.MAX_VALUE else 6,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp)
        )
    }
}


@Composable
private fun SpacerSmall() {
    Box(modifier = Modifier.padding(4.dp))
}

fun Context.copyToClipboard(
    label: String,
    text: String,
    toastMessage: String = "Copied to clipboard"
) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
}

@Composable
fun LogItemActionMenu(
    onCopy: () -> Unit,
    onCopyCurl: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {

        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Log actions"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Copy") },
                onClick = {
                    expanded = false
                    onCopy()
                }
            )

            DropdownMenuItem(
                text = { Text("Copy as cURL") },
                onClick = {
                    expanded = false
                    onCopyCurl()
                }
            )
        }
    }
}


