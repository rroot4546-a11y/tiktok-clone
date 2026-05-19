package com.tiktokclone.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiktokclone.data.models.Video
import com.tiktokclone.data.repository.VideoRepository
import com.tiktokclone.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val videos: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val isFollowingFeed: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isFollowingFeed = false, currentPage = 1)
            when (val result = videoRepository.getFeed(1)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        videos = result.data.videos,
                        isLoading = false,
                        hasMore = result.data.hasMore,
                        currentPage = 1,
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadFollowingFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isFollowingFeed = true, currentPage = 1)
            when (val result = videoRepository.getFollowingFeed(1)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        videos = result.data.videos,
                        isLoading = false,
                        hasMore = result.data.hasMore,
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || !state.hasMore) return

        viewModelScope.launch {
            val nextPage = state.currentPage + 1
            val result = if (state.isFollowingFeed) {
                videoRepository.getFollowingFeed(nextPage)
            } else {
                videoRepository.getFeed(nextPage)
            }

            when (result) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        videos = _uiState.value.videos + result.data.videos,
                        currentPage = nextPage,
                        hasMore = result.data.hasMore,
                    )
                }
                else -> {}
            }
        }
    }

    fun likeVideo(videoId: String) {
        viewModelScope.launch {
            videoRepository.likeVideo(videoId)
        }
    }

    fun saveVideo(videoId: String) {
        viewModelScope.launch {
            videoRepository.saveVideo(videoId)
        }
    }

    fun shareVideo(videoId: String) {
        viewModelScope.launch {
            videoRepository.shareVideo(videoId)
        }
    }
}
