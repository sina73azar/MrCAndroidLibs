package com.mrc.MrCAndroidLibs.data

import android.util.Log
import retrofit2.Response

/**
 * Mr.C 04/May/2026
 */
suspend fun <T> networkResolver(apiCall: suspend () -> Response<T>): ResolvedResult<T> {
    return try {
        val response = apiCall()
        resolveResponse(response)
    } catch (e: Exception) {
        Log.d("okhttp", "networkResolver: $e")
        ResolvedResult.Fail(e)
    }
}

suspend fun <T> resolveResponse(
    response: Response<T>
): ResolvedResult<T> {
    return try {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ResolvedResult.Success(
                    data = body,
                )
            } else {
                ResolvedResult.Error(
                    code = response.code(),
                    message = "Response body is null"
                )
            }
        } else {
            ResolvedResult.Error(
                code = response.code(),
                message = response.message(),
                errorBody = response.errorBody()?.string()
            )
        }
    } catch (e: Exception) {
        ResolvedResult.Fail(e)
    }
}