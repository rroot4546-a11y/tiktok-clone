package com.tiktokclone.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiktokclone.data.models.User
import com.tiktokclone.data.models.Video
import com.tiktokclone.data.repository.AuthRepository
import com.tiktokclone.data.repository.UserRepository
import com.tiktokclone.data.repository.VideoRepository
import com.tiktokclone.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val videos: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val videoRepository: VideoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = authRepository.getMe()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(user = result.data, isLoading = false)
                    loadUserVideos(result.data.id)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = userRepository.getProfile(userId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(user = result.data, isLoading = false)
                    loadUserVideos(userId)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    private fun loadUserVideos(userId: String) {
        viewModelScope.launch {
            when (val result = videoRepository.getUserVideos(userId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(videos = result.data.videos)
                }
                else -> {}
            }
        }
    }

    fun toggleFollow(userId: String) {
        viewModelScope.launch {
            when (val result = userRepository.followUser(userId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        user = _uiState.value.user?.copy(
                            isFollowing = result.data.isFollowing,
                            followersCount = result.data.followersCount,
                        )
                    )
                }
                else -> {}
            }
        }
    }
}
