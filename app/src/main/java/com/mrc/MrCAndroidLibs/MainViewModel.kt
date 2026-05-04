package com.mrc.MrCAndroidLibs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrc.MrCAndroidLibs.data.AppRepo
import com.mrc.MrCAndroidLibs.data.ResolvedResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Mr.C 04/May/2026
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val appRepo: AppRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState


    fun getPosts() {
        _uiState.update { uiState -> uiState.copy(fetchPostsOperation = ResolvedResult.Loading) }
        viewModelScope.launch {
            delay(3000)
            appRepo.getPosts().let {
                _uiState.update { uiState -> uiState.copy(fetchPostsOperation = it) }
            }
        }
    }
}