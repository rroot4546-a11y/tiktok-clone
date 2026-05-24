package com.tiktokclone.ui.video

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tiktokclone.data.repository.VideoRepository
import com.tiktokclone.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class UploadUiState(
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val uploadSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    application: Application,
    private val videoRepository: VideoRepository,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun uploadVideo(uri: Uri, caption: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, uploadProgress = 0f)

            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("upload_", ".mp4", context.cacheDir)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                _uiState.value = _uiState.value.copy(uploadProgress = 0.5f)

                when (val result = videoRepository.uploadVideo(tempFile, caption)) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isUploading = false,
                            uploadProgress = 1f,
                            uploadSuccess = true,
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isUploading = false,
                            error = result.message,
                        )
                    }
                    is Resource.Loading -> {}
                }

                tempFile.delete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    error = e.message,
                )
            }
        }
    }
}
