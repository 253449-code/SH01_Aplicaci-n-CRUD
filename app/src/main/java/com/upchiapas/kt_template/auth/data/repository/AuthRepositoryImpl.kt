package com.upchiapas.kt_template.auth.data.repository

import com.upchiapas.kt_template.auth.data.datasource.AuthApiService
import com.upchiapas.kt_template.auth.data.dto.LoginRequestDto
import com.upchiapas.kt_template.auth.data.dto.RegisterRequestDto
import com.upchiapas.kt_template.auth.data.mapper.toDomain
import com.upchiapas.kt_template.auth.domain.model.AuthSession
import com.upchiapas.kt_template.auth.domain.repository.AuthRepository
import com.upchiapas.kt_template.auth.domain.repository.SessionStorage
import com.upchiapas.kt_template.core.network.ErrorMapper
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val sessionStorage: SessionStorage,
    private val errorMapper: ErrorMapper
) : AuthRepository {

    override val sessionFlow: Flow<AuthSession?> = sessionStorage.sessionFlow

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<AuthSession> {
        val result = runCatching {
            val request = RegisterRequestDto(
                name = name.trim(),
                email = email.trim(),
                password = password
            )
            val session = apiService.register(request).toDomain()
            sessionStorage.saveSession(session)
            session
        }
        return errorMapper.mapResult(result)
    }

    override suspend fun login(email: String, password: String): Result<AuthSession> {
        val result = runCatching {
            val request = LoginRequestDto(
                email = email.trim(),
                password = password
            )
            val session = apiService.login(request).toDomain()
            sessionStorage.saveSession(session)
            session
        }
        return errorMapper.mapResult(result)
    }

    override suspend fun logout() {
        sessionStorage.clearSession()
    }
}
