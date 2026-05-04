package com.mrc.MrCAndroidLibs.data

/**
 * Mr.C 04/May/2026
 */
sealed class ResolvedResult<out T> {
    data class Success<T>(val data: T) : ResolvedResult<T>()
    data class Error(
        val code: Int,
        val message: String?,
        val errorBody: String? = null
    ) : ResolvedResult<Nothing>()

    data class Fail(
        val throwable: Throwable
    ) : ResolvedResult<Nothing>()

    object Loading : ResolvedResult<Nothing>()
    object Idle : ResolvedResult<Nothing>()
}