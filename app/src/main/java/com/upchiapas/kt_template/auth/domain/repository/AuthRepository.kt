package com.upchiapas.kt_template.auth.domain.repository

import com.upchiapas.kt_template.auth.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionFlow: Flow<AuthSession?>
    suspend fun register(name: String, email: String, password: String): Result<AuthSession>
    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun logout()
}
