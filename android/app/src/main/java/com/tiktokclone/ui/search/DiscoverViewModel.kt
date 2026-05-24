package com.tiktokclone.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiktokclone.data.models.Hashtag
import com.tiktokclone.data.models.User
import com.tiktokclone.data.models.Video
import com.tiktokclone.data.repository.UserRepository
import com.tiktokclone.data.repository.VideoRepository
import com.tiktokclone.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val trendingHashtags: List<Hashtag> = emptyList(),
    val trendingVideos: List<Video> = emptyList(),
    val suggestedUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val videoRepository: VideoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        loadDiscover()
    }

    private fun loadDiscover() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            launch {
                when (val result = videoRepository.getTrendingVideos()) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(trendingVideos = result.data.videos)
                    }
                    else -> {}
                }
            }

            launch {
                when (val result = userRepository.getSuggestedUsers()) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(suggestedUsers = result.data)
                    }
                    else -> {}
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
