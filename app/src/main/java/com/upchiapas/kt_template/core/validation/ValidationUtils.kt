package com.upchiapas.kt_template.core.validation

import java.time.LocalDate

object ValidationUtils {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val DATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email.trim())
    }

    fun isValidIsoDate(value: String): Boolean {
        val trimmed = value.trim()
        if (!DATE_REGEX.matches(trimmed)) return false
        return runCatching { LocalDate.parse(trimmed) }.isSuccess
    }
}
