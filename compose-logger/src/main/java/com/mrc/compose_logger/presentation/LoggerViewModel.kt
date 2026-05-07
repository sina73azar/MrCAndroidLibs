package com.mrc.compose_logger.presentation

import androidx.lifecycle.ViewModel
import com.mrc.compose_logger.data.LogFilterState
import com.mrc.compose_logger.data.NetworkLogEntry
import com.mrc.compose_logger.data.StatusFilter
import com.mrc.compose_logger.di.LoggerGraph
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LoggerViewModel @Inject constructor(

) : ViewModel() {

    private val loggerStore = LoggerGraph.loggerStore

    // ✅ use filtered logs
    val logs: StateFlow<List<NetworkLogEntry>> = loggerStore.filteredLogs
    val filterState: StateFlow<LogFilterState> = loggerStore.filterState

    fun updateQuery(query: String) = loggerStore.updateQuery(query)
    fun updateMethod(method: String?) = loggerStore.updateMethod(method)
    fun updateStatus(status: StatusFilter) = loggerStore.updateStatus(status)

    fun clear() = loggerStore.clear()
}