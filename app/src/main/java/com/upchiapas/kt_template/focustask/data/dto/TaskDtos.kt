package com.upchiapas.kt_template.focustask.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: Int,
    val title: String,
    val description: String = "",
    val category: String,
    @SerialName("estimated_time") val estimatedTime: Double,
    val priority: String,
    @SerialName("due_date") val dueDate: String,
    @SerialName("is_completed") val isCompleted: Boolean = false,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class CreateTaskDto(
    val title: String,
    val description: String,
    val category: String,
    @SerialName("estimated_time") val estimatedTime: Double,
    val priority: String,
    @SerialName("due_date") val dueDate: String,
    @SerialName("is_completed") val isCompleted: Boolean
)

@Serializable
data class UpdateTaskDto(
    val title: String,
    val description: String,
    val category: String,
    @SerialName("estimated_time") val estimatedTime: Double,
    val priority: String,
    @SerialName("due_date") val dueDate: String,
    @SerialName("is_completed") val isCompleted: Boolean
)
