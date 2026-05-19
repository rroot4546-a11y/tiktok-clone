package com.tiktokclone.ui.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.tiktokclone.data.auth.GoogleSignInHelper
import com.tiktokclone.data.models.User
import com.tiktokclone.data.repository.AuthRepository
import com.tiktokclone.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val googleSignInHelper = GoogleSignInHelper(context)

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                when (val result = authRepository.getMe()) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(isLoggedIn = true, user = result.data)
                    }
                    else -> {
                        _uiState.value = _uiState.value.copy(isLoggedIn = false)
                    }
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.login(email, password)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = result.data.user,
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.register(username, email, password)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = result.data.user,
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun handleGoogleSignInResult(result: ActivityResult) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = googleSignInHelper.handleSignInResult(task)

                if (account != null) {
                    val authResult = googleSignInHelper.firebaseAuthWithGoogle(account)
                    if (authResult.success) {
                        when (val apiResult = authRepository.googleAuth(
                            idToken = authResult.idToken,
                            email = authResult.email,
                            displayName = authResult.displayName,
                            photoUrl = authResult.photoUrl,
                            googleId = authResult.googleId,
                        )) {
                            is Resource.Success -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    isLoggedIn = true,
                                    user = apiResult.data.user,
                                )
                            }
                            is Resource.Error -> {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    error = apiResult.message,
                                )
                            }
                            is Resource.Loading -> {}
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = authResult.error ?: "Google sign-in failed",
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Google sign-in cancelled",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Google sign-in failed",
                )
            }
        }
    }

    fun getGoogleSignInIntent(): Intent = googleSignInHelper.getSignInIntent()

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            authRepository.forgotPassword(email)
        }
    }

    fun logout() {
        viewModelScope.launch {
            googleSignInHelper.signOut()
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }
}
