package com.upchiapas.kt_template.auth.data.mapper

import com.upchiapas.kt_template.auth.data.dto.AuthResponseDto
import com.upchiapas.kt_template.auth.data.dto.UserDto
import com.upchiapas.kt_template.auth.domain.model.AuthSession
import com.upchiapas.kt_template.auth.domain.model.User

fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email
    )
}

fun AuthResponseDto.toDomain(): AuthSession {
    return AuthSession(
        token = accessToken,
        user = user.toDomain()
    )
}
