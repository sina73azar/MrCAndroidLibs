package com.mrc.MrCAndroidLibs.data

import android.util.Log
import javax.inject.Inject

/**
 * Mr.C 04/May/2026
 */
class AppRepoImpl @Inject constructor(
    val apiService: ApiService
) : AppRepo {
    override suspend fun getPosts(): ResolvedResult<PostsResponse> {
        return networkResolver {
            val res = apiService.getPosts()
            Log.d("okhttp", "getPosts: $res")
            res
            /*Response.success(
                listOf()
            )*/
        }
    }
}

