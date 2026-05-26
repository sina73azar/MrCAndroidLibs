package com.mrc.MrCAndroidLibs.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Mr.C 04/May/2026
 */
interface ApiService {

    @GET("posts")
    suspend fun getPosts(): Response<PostsResponse>

    @POST
    suspend fun registerFcmToken(
        @Url url: String,
        @Body request: FcmRegistrationRequest
    ): Response<Unit>
}
