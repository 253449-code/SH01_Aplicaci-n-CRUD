package com.upchiapas.kt_template.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.kt_template.auth.domain.repository.AuthRepository
import com.upchiapas.kt_template.auth.presentation.screens.SessionUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SessionViewModel(
    authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<SessionUiState> = authRepository.sessionFlow
        .map { session ->
            if (session != null) {
                SessionUiState.Authenticated(session)
            } else {
                SessionUiState.Unauthenticated
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionUiState.Loading
        )
}
