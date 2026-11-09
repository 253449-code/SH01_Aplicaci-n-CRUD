package com.upchiapas.kt_template.auth.domain.repository

import com.upchiapas.kt_template.auth.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface SessionStorage {
    val sessionFlow: Flow<AuthSession?>
    suspend fun getToken(): String?
    suspend fun saveSession(session: AuthSession)
    suspend fun clearSession()
}
