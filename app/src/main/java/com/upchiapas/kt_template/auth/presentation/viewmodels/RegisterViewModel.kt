package com.upchiapas.kt_template.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.kt_template.auth.domain.repository.AuthRepository
import com.upchiapas.kt_template.auth.presentation.screens.RegisterUiState
import com.upchiapas.kt_template.core.validation.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update {
            it.copy(name = value, nameError = null, errorMessage = null)
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(email = value, emailError = null, errorMessage = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, passwordError = null, confirmPasswordError = null, errorMessage = null)
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null)
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun register() {
        val current = _uiState.value
        val nameTrimmed = current.name.trim()
        val emailTrimmed = current.email.trim()

        val nameError = when {
            nameTrimmed.isEmpty() -> "El nombre es obligatorio"
            nameTrimmed.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
        }

        val emailError = when {
            emailTrimmed.isEmpty() -> "El correo es obligatorio"
            !ValidationUtils.isValidEmail(emailTrimmed) -> "Ingresa un correo electrónico válido"
            else -> null
        }

        val passwordError = when {
            current.password.isBlank() -> "La contraseña es obligatoria"
            current.password.length < 8 -> "La contraseña debe tener mínimo 8 caracteres"
            else -> null
        }

        val confirmPasswordError = when {
            current.confirmPassword.isBlank() -> "Confirma tu contraseña"
            current.confirmPassword != current.password -> "Las contraseñas no coinciden"
            else -> null
        }

        if (nameError != null || emailError != null || passwordError != null || confirmPasswordError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authRepository.register(
                name = nameTrimmed,
                email = emailTrimmed,
                password = current.password
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "No se pudo completar el registro"
                        )
                    }
                }
            )
        }
    }
}
