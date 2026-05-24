package com.tiktokclone.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiktokclone.data.models.Hashtag
import com.tiktokclone.data.models.User
import com.tiktokclone.data.models.Video
import com.tiktokclone.data.repository.UserRepository
import com.tiktokclone.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val users: List<User> = emptyList(),
    val videos: List<Video> = emptyList(),
    val hashtags: List<Hashtag> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = SearchUiState()
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // debounce
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = userRepository.search(query)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        users = result.data.users ?: emptyList(),
                        videos = result.data.videos ?: emptyList(),
                        hashtags = result.data.hashtags ?: emptyList(),
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
}
