package com.mrc.MrCAndroidLibs.data

import retrofit2.Response
import retrofit2.http.GET

/**
 * Mr.C 04/May/2026
 */
interface ApiService {

    @GET("/posts")
    suspend fun getPosts(): Response<List<Post>>
}