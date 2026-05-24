package com.tiktokclone.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiktokclone.data.models.Comment
import com.tiktokclone.data.repository.VideoRepository
import com.tiktokclone.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val total: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    fun loadComments(videoId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = videoRepository.getComments(videoId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        comments = result.data.comments,
                        total = result.data.total,
                        isLoading = false,
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun addComment(videoId: String, text: String) {
        viewModelScope.launch {
            when (val result = videoRepository.addComment(videoId, text)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        comments = listOf(result.data) + _uiState.value.comments,
                        total = _uiState.value.total + 1,
                    )
                }
                else -> {}
            }
        }
    }

    fun likeComment(commentId: String) {
        viewModelScope.launch {
            videoRepository.likeComment(commentId)
        }
    }
}
