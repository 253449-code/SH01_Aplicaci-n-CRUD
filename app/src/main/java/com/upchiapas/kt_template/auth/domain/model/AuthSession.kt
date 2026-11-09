package com.upchiapas.kt_template.auth.domain.model

data class AuthSession(
    val token: String,
    val user: User
)
