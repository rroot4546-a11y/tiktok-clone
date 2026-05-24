package com.tiktokclone.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiktokclone.data.local.TokenManager
import com.tiktokclone.data.models.Message
import com.tiktokclone.data.repository.ChatRepository
import com.tiktokclone.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val currentUserId: String = "",
    val error: String? = null,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: ""
            _uiState.value = _uiState.value.copy(currentUserId = userId)
        }
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            chatRepository.markAsRead(conversationId)
            when (val result = chatRepository.getMessages(conversationId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(messages = result.data, isLoading = false)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch {
            when (val result = chatRepository.sendMessage(conversationId, content)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + result.data,
                    )
                }
                else -> {}
            }
        }
    }
}
