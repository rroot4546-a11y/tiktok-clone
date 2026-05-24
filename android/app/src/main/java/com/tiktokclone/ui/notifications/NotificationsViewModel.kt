package com.tiktokclone.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiktokclone.data.models.Notification
import com.tiktokclone.data.repository.UserRepository
import com.tiktokclone.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = userRepository.getNotifications()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        notifications = result.data.notifications,
                        unreadCount = result.data.unreadCount,
                        isLoading = false,
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            userRepository.markAllNotificationsAsRead()
            _uiState.value = _uiState.value.copy(
                notifications = _uiState.value.notifications.map { it.copy(isRead = true) },
                unreadCount = 0,
            )
        }
    }
}
