package com.upchiapas.kt_template.focustask.presentation.screens

import com.upchiapas.kt_template.focustask.domain.model.Task

data class TasksHomeUiState(
    val userName: String = "",
    val tasks: List<Task> = emptyList(),
    val showCompleted: Boolean = true,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
    val taskToDelete: Task? = null
) {
    val visibleTasks: List<Task>
        get() = if (showCompleted) tasks else tasks.filter { !it.isCompleted }

    val completedCount: Int
        get() = tasks.count { it.isCompleted }

    val totalCount: Int
        get() = tasks.size

    val totalPoints: Int
        get() = tasks.filter { it.isCompleted }.sumOf { it.points }
}
