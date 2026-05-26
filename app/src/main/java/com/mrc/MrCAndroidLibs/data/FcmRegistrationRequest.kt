package com.mrc.MrCAndroidLibs.data

import kotlinx.serialization.Serializable

@Serializable
data class FcmRegistrationRequest(
    val token: String,
    val platform: String = "android"
)
