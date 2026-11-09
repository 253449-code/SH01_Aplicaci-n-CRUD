package com.upchiapas.kt_template.focustask.data.mapper

import com.upchiapas.kt_template.focustask.data.dto.CreateTaskDto
import com.upchiapas.kt_template.focustask.data.dto.TaskDto
import com.upchiapas.kt_template.focustask.data.dto.UpdateTaskDto
import com.upchiapas.kt_template.focustask.domain.model.Task

fun TaskDto.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        category = category,
        estimatedTime = estimatedTime,
        priority = priority,
        dueDate = dueDate,
        isCompleted = isCompleted,
        createdAt = createdAt
    )
}

fun List<TaskDto>.toDomainList(): List<Task> {
    return map { it.toDomain() }
}

fun Task.toCreateDto(): CreateTaskDto {
    return CreateTaskDto(
        title = title,
        description = description,
        category = category,
        estimatedTime = estimatedTime,
        priority = priority,
        dueDate = dueDate,
        isCompleted = isCompleted
    )
}

fun Task.toUpdateDto(): UpdateTaskDto {
    return UpdateTaskDto(
        title = title,
        description = description,
        category = category,
        estimatedTime = estimatedTime,
        priority = priority,
        dueDate = dueDate,
        isCompleted = isCompleted
    )
}
