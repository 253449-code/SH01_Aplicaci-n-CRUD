package com.upchiapas.kt_template.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.kt_template.auth.domain.repository.AuthRepository
import com.upchiapas.kt_template.auth.presentation.screens.LoginUiState
import com.upchiapas.kt_template.core.validation.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun login() {
        val current = _uiState.value
        val emailTrimmed = current.email.trim()

        val emailError = when {
            emailTrimmed.isEmpty() -> "El correo es obligatorio"
            !ValidationUtils.isValidEmail(emailTrimmed) -> "Ingresa un correo electrónico válido"
            else -> null
        }

        val passwordError = when {
            current.password.isBlank() -> "La contraseña es obligatoria"
            current.password.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
            else -> null
        }

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.login(email = emailTrimmed, password = current.password)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "No se pudo iniciar sesión"
                        )
                    }
                }
            )
        }
    }
}
