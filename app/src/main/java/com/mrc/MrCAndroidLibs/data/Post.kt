package com.mrc.MrCAndroidLibs.data

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int,
    val title: String
)
