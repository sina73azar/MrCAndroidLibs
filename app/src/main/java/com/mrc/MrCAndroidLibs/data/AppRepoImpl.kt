package com.mrc.MrCAndroidLibs.data

import android.util.Log
import javax.inject.Inject

/**
 * Mr.C 04/May/2026
 */
class AppRepoImpl @Inject constructor(
    private val apiService: ApiService,
    private val networkProxySmokeTest: NetworkProxySmokeTest
) : AppRepo {
    override suspend fun getPosts(): ResolvedResult<PostsResponse> {
        return networkResolver {
            val proxySession = networkProxySmokeTest.ensureStarted()
            Log.d(
                "okhttp",
                "getPosts: proxy session ready at ${proxySession.host}:${proxySession.port}"
            )
            val res = apiService.getPosts()
            Log.d("okhttp", "getPosts: $res")
            res
        }
    }
}
