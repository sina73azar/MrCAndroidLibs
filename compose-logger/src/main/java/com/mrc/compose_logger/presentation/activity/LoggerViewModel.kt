package com.mrc.compose_logger.presentation.activity

import androidx.lifecycle.ViewModel
import com.mrc.compose_logger.core.LoggerGraph
import com.mrc.compose_logger.core.filters.LogFilterState
import com.mrc.compose_logger.core.filters.StatusFilter
import com.mrc.compose_logger.core.models.NetworkLogEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LoggerViewModel @Inject constructor(

) : ViewModel() {

    private val loggerStore = LoggerGraph.store()

    // ✅ use filtered logs
    val logs: StateFlow<List<NetworkLogEntry>> = loggerStore.filteredLogs
    val filterState: StateFlow<LogFilterState> = loggerStore.filterState

    fun updateQuery(query: String) = loggerStore.updateQuery(query)
    fun updateMethod(method: String?) = loggerStore.updateMethod(method)
    fun updateStatus(status: StatusFilter) = loggerStore.updateStatus(status)

    fun clear() = loggerStore.clear()
}