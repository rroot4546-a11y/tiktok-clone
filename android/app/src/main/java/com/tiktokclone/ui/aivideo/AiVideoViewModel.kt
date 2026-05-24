package com.tiktokclone.ui.aivideo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiktokclone.data.api.VeoAiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiVideoUiState(
    val prompt: String = "",
    val isGenerating: Boolean = false,
    val progressMessage: String = "",
    val videoUrl: String? = null,
    val error: String? = null,
)

@HiltViewModel
class AiVideoViewModel @Inject constructor(
    private val veoAiService: VeoAiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiVideoUiState())
    val uiState: StateFlow<AiVideoUiState> = _uiState.asStateFlow()

    fun updatePrompt(prompt: String) {
        _uiState.value = _uiState.value.copy(prompt = prompt)
    }

    fun generateVideo() {
        val prompt = _uiState.value.prompt.trim()
        if (prompt.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a prompt")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGenerating = true,
                error = null,
                videoUrl = null,
                progressMessage = "Starting...",
            )

            val result = veoAiService.generateFullVideo(prompt) { progress ->
                _uiState.value = _uiState.value.copy(progressMessage = progress)
            }

            result.fold(
                onSuccess = { url ->
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        videoUrl = url,
                        progressMessage = "Video ready!",
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        error = error.message ?: "Failed to generate video",
                        progressMessage = "",
                    )
                },
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = AiVideoUiState()
    }
}
