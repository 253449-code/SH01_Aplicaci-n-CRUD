package com.upchiapas.kt_template.focustask.data.repository

import com.upchiapas.kt_template.core.network.ErrorMapper
import com.upchiapas.kt_template.focustask.data.datasource.TasksApiService
import com.upchiapas.kt_template.focustask.data.mapper.toCreateDto
import com.upchiapas.kt_template.focustask.data.mapper.toDomain
import com.upchiapas.kt_template.focustask.data.mapper.toDomainList
import com.upchiapas.kt_template.focustask.data.mapper.toUpdateDto
import com.upchiapas.kt_template.focustask.domain.model.Task
import com.upchiapas.kt_template.focustask.domain.repository.TaskRepository

class TaskRepositoryImpl(
    private val apiService: TasksApiService,
    private val errorMapper: ErrorMapper
) : TaskRepository {

    override suspend fun getTasks(): Result<List<Task>> {
        val result = runCatching {
            apiService.getTasks().toDomainList()
        }
        return errorMapper.mapResult(result)
    }

    override suspend fun getTaskById(id: Int): Result<Task> {
        val result = runCatching {
            apiService.getTaskById(id).toDomain()
        }
        return errorMapper.mapResult(result)
    }

    override suspend fun createTask(task: Task): Result<Task> {
        val result = runCatching {
            apiService.createTask(task.toCreateDto()).toDomain()
        }
        return errorMapper.mapResult(result)
    }

    override suspend fun updateTask(task: Task): Result<Task> {
        val result = runCatching {
            apiService.updateTask(task.id, task.toUpdateDto()).toDomain()
        }
        return errorMapper.mapResult(result)
    }

    override suspend fun deleteTask(id: Int): Result<Unit> {
        val result = runCatching {
            apiService.deleteTask(id)
            Unit
        }
        return errorMapper.mapResult(result)
    }
}
