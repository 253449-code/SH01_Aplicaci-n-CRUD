package com.upchiapas.kt_template.focustask.presentation.screens

import com.upchiapas.kt_template.focustask.domain.model.Task

data class TaskDetailUiState(
    val task: Task? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
    val deletedSuccessMessage: String? = null
)
