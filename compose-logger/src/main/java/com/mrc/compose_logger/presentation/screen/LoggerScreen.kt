package com.mrc.compose_logger.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrc.compose_logger.core.filters.StatusFilter
import com.mrc.compose_logger.presentation.activity.LoggerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggerScreen(
    viewModel: LoggerViewModel = hiltViewModel()
) {
    val logs by viewModel.logs.collectAsState()
    val filter by viewModel.filterState.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showFilters by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    var shouldAutoScroll by remember { mutableStateOf(true) }

    // ✅ Detect user scroll
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisible =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisible == listState.layoutInfo.totalItemsCount - 1
        }.collect { atBottom ->
            shouldAutoScroll = atBottom
        }
    }

    LaunchedEffect(Unit) {
        if (filter.query.isNotEmpty()) {
            showSearch = true
        }
        if (filter.status != StatusFilter.ALL || filter.method != null) {
            showFilters = true
        }

    }

    Scaffold(
        topBar = {
            LoggerTopBar(
                onSearchClick = { showSearch = !showSearch },
                onToggleFilters = { showFilters = !showFilters },
                onClearLogs = viewModel::clear
            )
        }
    ) { padding ->

        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(bottom = 8.dp)
            ) {

                // 🔍 Search
                AnimatedVisibility(visible = showSearch) {
                    LoggerSearchSection(
                        query = filter.query,
                        onQueryChange = viewModel::updateQuery,
                        onClose = { showSearch = false }
                    )
                }

                // 🎛 Filters
                AnimatedVisibility(visible = showFilters) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))

                        CompactMethodRow(
                            selected = filter.method,
                            onSelect = viewModel::updateMethod
                        )

                        Spacer(Modifier.height(6.dp))

                        CompactStatusRow(
                            selected = filter.status,
                            onSelect = viewModel::updateStatus
                        )

                        Spacer(Modifier.height(8.dp))
                    }
                }

                // 📜 Logs
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 8.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = logs.reversed(),
                        key = { it.id }
                    ) { log ->

                        NetworkLogItem(log)

                    }
                }
            }
            // ⬇ Jump to latest
            if (!shouldAutoScroll && logs.isNotEmpty()) {

                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(logs.lastIndex)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Jump to latest"
                    )
                }
            }
        }
    }

    // ✅ Auto-scroll
    /*    LaunchedEffect(logs.size) {
            if (logs.isNotEmpty() && shouldAutoScroll) {
                listState.scrollToItem(logs.lastIndex)
            }
        }*/
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompactMethodRow(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    val methods = listOf("ALL", "GET", "POST", "PUT", "DELETE")

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        methods.forEach { method ->
            MethodChip(
                method = method,
                selected = selected == method,
                onClick = {
                    onSelect(if (selected == method || method == "ALL") null else method)
                }
            )
        }
    }
}

@Composable
fun MethodChip(
    modifier: Modifier = Modifier,
    method: String,
    selected: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val color = when (method.uppercase()) {
        "All" -> MaterialTheme.colorScheme.surface
        "GET" -> Color(0xFF4CAF50)
        "POST" -> Color(0xFF2196F3)
        "PUT" -> Color(0xFFFF9800)
        "DELETE" -> Color(0xFFF44336)
        "PATCH" -> Color(0xFF9C27B0)
        else -> MaterialTheme.colorScheme.outline
    }

    AssistChip(
        modifier = modifier,
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        label = {
            Text(
                text = method.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = if (selected) 0.15f else 0.08f),
            labelColor = color
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = color.copy(alpha = 0.5f)
        )
    )
}


@Composable
fun CompactStatusRow(
    selected: StatusFilter,
    onSelect: (StatusFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        StatusFilter.entries.forEach { status ->

            val color = when (status) {
                StatusFilter.SUCCESS_2XX -> Color(0xFF2E7D32)
                StatusFilter.CLIENT_4XX -> Color(0xFFEF6C00)
                StatusFilter.SERVER_5XX -> Color(0xFFC62828)
                StatusFilter.ERROR -> Color(0xFFB00020)
                StatusFilter.ALL -> MaterialTheme.colorScheme.primary
            }

            AssistChip(
                onClick = { onSelect(status) },
                label = { Text(status.displayName) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected == status)
                        color.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}


