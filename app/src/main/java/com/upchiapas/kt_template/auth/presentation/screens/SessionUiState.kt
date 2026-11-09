package com.upchiapas.kt_template.auth.presentation.screens

import com.upchiapas.kt_template.auth.domain.model.AuthSession

sealed interface SessionUiState {
    data object Loading : SessionUiState
    data object Unauthenticated : SessionUiState
    data class Authenticated(val session: AuthSession) : SessionUiState
}
