package com.upchiapas.kt_template.focustask.domain.repository

import com.upchiapas.kt_template.focustask.domain.model.Task

interface TaskRepository {
    suspend fun getTasks(): Result<List<Task>>
    suspend fun getTaskById(id: Int): Result<Task>
    suspend fun createTask(task: Task): Result<Task>
    suspend fun updateTask(task: Task): Result<Task>
    suspend fun deleteTask(id: Int): Result<Unit>
}
