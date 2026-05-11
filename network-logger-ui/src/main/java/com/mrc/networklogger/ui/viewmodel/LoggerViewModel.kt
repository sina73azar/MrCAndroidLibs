package com.mrc.networklogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.mrc.networklogger.core.filter.LogFilterState
import com.mrc.networklogger.core.filter.StatusFilter
import com.mrc.networklogger.core.internal.ServiceLocator
import com.mrc.networklogger.core.model.NetworkLogEntry
import kotlinx.coroutines.flow.StateFlow

class LoggerViewModel() : ViewModel() {

    private val loggerStore = ServiceLocator.store()

    // ✅ use filtered logs
    val logs: StateFlow<List<NetworkLogEntry>> = loggerStore.filteredLogs
    val filterState: StateFlow<LogFilterState> = loggerStore.filterState

    fun updateQuery(query: String) = loggerStore.updateQuery(query)
    fun updateMethod(method: String?) = loggerStore.updateMethod(method)
    fun updateStatus(status: StatusFilter) = loggerStore.updateStatus(status)

    fun clear() = loggerStore.clear()
}