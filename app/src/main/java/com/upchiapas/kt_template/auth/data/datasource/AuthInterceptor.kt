package com.upchiapas.kt_template.auth.data.datasource

import com.upchiapas.kt_template.auth.domain.repository.SessionStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionStorage: SessionStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        val isAuthEndpoint = path.endsWith("/login") || path.endsWith("/register")

        val token = if (!isAuthEndpoint) {
            runBlocking { sessionStorage.getToken() }
        } else {
            null
        }

        val request = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(request)

        if (response.code == 401 && !isAuthEndpoint) {
            runBlocking { sessionStorage.clearSession() }
        }

        return response
    }
}
