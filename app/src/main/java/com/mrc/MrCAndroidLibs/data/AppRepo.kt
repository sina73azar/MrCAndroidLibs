package com.mrc.MrCAndroidLibs.data

/**
 * Mr.C 04/May/2026
 */
interface AppRepo {
    suspend fun getPosts(): ResolvedResult<List<Post>>
}
