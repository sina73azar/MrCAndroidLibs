package com.mrc.MrCAndroidLibs.data

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int,
    val title: String,
    val body: String
)

@Serializable
data class PostsResponse(
    val posts: List<Post>,
)