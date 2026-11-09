package com.upchiapas.kt_template

import com.upchiapas.kt_template.auth.data.dto.AuthResponseDto
import com.upchiapas.kt_template.auth.data.dto.UserDto
import com.upchiapas.kt_template.auth.data.mapper.toDomain
import com.upchiapas.kt_template.focustask.data.dto.TaskDto
import com.upchiapas.kt_template.focustask.data.mapper.toCreateDto
import com.upchiapas.kt_template.focustask.data.mapper.toDomain
import com.upchiapas.kt_template.focustask.data.mapper.toUpdateDto
import com.upchiapas.kt_template.focustask.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    @Test
    fun taskDto_mapsToDomainAndBack() {
        val dto = TaskDto(
            id = 10,
            title = "Practicar Kotlin",
            description = "Hacer pruebas unitarias",
            category = "Estudio",
            estimatedTime = 3.0,
            priority = "Alta",
            dueDate = "2026-11-30",
            isCompleted = true,
            createdAt = "2026-11-09 12:00:00"
        )

        val domain = dto.toDomain()
        assertEquals(10, domain.id)
        assertEquals("Practicar Kotlin", domain.title)
        assertEquals(3.0, domain.estimatedTime, 0.001)
        assertTrue(domain.isCompleted)

        val createDto = domain.toCreateDto()
        assertEquals(domain.title, createDto.title)
        assertEquals(domain.dueDate, createDto.dueDate)

        val updateDto = domain.toUpdateDto()
        assertEquals(domain.priority, updateDto.priority)
        assertEquals(domain.isCompleted, updateDto.isCompleted)
    }

    @Test
    fun authResponseDto_mapsToAuthSession() {
        val responseDto = AuthResponseDto(
            accessToken = "jwt.token.value",
            tokenType = "bearer",
            user = UserDto(id = 4, name = "Emma", email = "emma@test.com")
        )

        val session = responseDto.toDomain()
        assertEquals("jwt.token.value", session.token)
        assertEquals(4, session.user.id)
        assertEquals("Emma", session.user.name)
        assertEquals("emma@test.com", session.user.email)
    }

    @Test
    fun taskPointsCalculation_isConsistent() {
        val task = Task(
            title = "API",
            category = "Trabajo",
            estimatedTime = 2.0,
            priority = "Alta",
            dueDate = "2026-11-20"
        )
        assertEquals(80, task.points)
    }
}
