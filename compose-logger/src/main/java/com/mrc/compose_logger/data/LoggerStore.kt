package com.mrc.compose_logger.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggerStore {

    private val _networkLogs =
        MutableStateFlow<List<NetworkLogEntry>>(emptyList())
    val networkLogs: StateFlow<List<NetworkLogEntry>> = _networkLogs

    private val _filterState = MutableStateFlow(LogFilterState())
    val filterState: StateFlow<LogFilterState> = _filterState


    val filteredLogs: StateFlow<List<NetworkLogEntry>> =
        combine(_networkLogs, _filterState) { logs, filter ->
            logs.filter { log ->
                matchesQuery(log, filter) &&
                        matchesMethod(log, filter) &&
                        matchesStatus(log, filter)
            }
        }.stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun add(entry: NetworkLogEntry) {
        _networkLogs.update { it + entry }
    }

    fun updateResponse(
        id: String,
        response: ResponseLogModel,
        durationMs: Long
    ) {
        _networkLogs.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(response = response, durationMs = durationMs)
                } else it
            }
        }
    }

    fun updateError(id: String, error: String) {
        _networkLogs.update { list ->
            list.map {
                if (it.id == id) it.copy(error = error) else it
            }
        }
    }

    fun updateQuery(query: String) {
        _filterState.update { it.copy(query = query) }
    }

    fun updateMethod(method: String?) {
        _filterState.update { it.copy(method = method) }
    }

    fun updateStatus(status: StatusFilter) {
        _filterState.update { it.copy(status = status) }
    }

    fun clear() {
        _networkLogs.value = emptyList()
    }
}

private fun matchesQuery(
    log: NetworkLogEntry,
    filter: LogFilterState
): Boolean {
    if (filter.query.isBlank()) return true
    return log.request.url.contains(filter.query, ignoreCase = true)
}

private fun matchesMethod(
    log: NetworkLogEntry,
    filter: LogFilterState
): Boolean {
    return filter.method == null || log.request.method == filter.method
}

private fun matchesStatus(
    log: NetworkLogEntry,
    filter: LogFilterState
): Boolean {
    return when (filter.status) {
        StatusFilter.ALL -> true
        StatusFilter.SUCCESS_2XX -> log.response?.code in 200..299
        StatusFilter.CLIENT_4XX -> log.response?.code in 400..499
        StatusFilter.SERVER_5XX -> log.response?.code in 500..599
        StatusFilter.ERROR -> log.response == null || log.error != null
    }
}