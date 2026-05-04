package com.mrc.MrCAndroidLibs

import com.mrc.MrCAndroidLibs.data.Post
import com.mrc.MrCAndroidLibs.data.ResolvedResult

/**
 * Mr.C 04/May/2026
 */
data class UiState(
    val fetchPostsOperation: ResolvedResult<List<Post>> = ResolvedResult.Idle
)
