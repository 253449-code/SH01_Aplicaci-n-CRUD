package com.upchiapas.kt_template.focustask.presentation.screens

import com.upchiapas.kt_template.focustask.domain.model.Task
import java.time.LocalDate

data class TaskFormUiState(
    val isEditMode: Boolean = false,
    val title: String = "",
    val description: String = "",
    val category: String = Task.CATEGORIES.first(),
    val estimatedTimeText: String = "2.0",
    val priority: String = "Media",
    val dueDate: String = LocalDate.now().toString(),
    val selectedDateMillis: Long? = null,
    val isCompleted: Boolean = false,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val estimatedTimeError: String? = null,
    val dueDateError: String? = null,
    val isLoadingInitialData: Boolean = false,
    val loadErrorMessage: String? = null,
    val isSaving: Boolean = false,
    val showDatePicker: Boolean = false,
    val errorMessage: String? = null,
    val savedMessage: String? = null
)
