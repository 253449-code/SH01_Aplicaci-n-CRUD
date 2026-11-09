package com.upchiapas.kt_template.focustask.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.kt_template.core.validation.ValidationUtils
import com.upchiapas.kt_template.focustask.domain.model.Task
import com.upchiapas.kt_template.focustask.domain.repository.TaskRepository
import com.upchiapas.kt_template.focustask.presentation.screens.TaskFormUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class TaskFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository
) : ViewModel() {

    private val taskId: Int? = savedStateHandle.get<Int>("taskId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(
        TaskFormUiState(
            isEditMode = taskId != null,
            selectedDateMillis = parseDateToUtcMillis(LocalDate.now().toString())
        )
    )
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    init {
        if (taskId != null) {
            loadTask(taskId)
        }
    }

    fun retryLoad() {
        taskId?.let { loadTask(it) }
    }

    private fun loadTask(id: Int) {
        _uiState.update { it.copy(isLoadingInitialData = true, loadErrorMessage = null) }
        viewModelScope.launch {
            val result = repository.getTaskById(id)
            result.fold(
                onSuccess = { task ->
                    _uiState.update {
                        it.copy(
                            isLoadingInitialData = false,
                            title = task.title,
                            description = task.description,
                            category = task.category,
                            estimatedTimeText = task.estimatedTime.toString(),
                            priority = task.priority,
                            dueDate = task.dueDate,
                            selectedDateMillis = parseDateToUtcMillis(task.dueDate),
                            isCompleted = task.isCompleted,
                            loadErrorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingInitialData = false,
                            loadErrorMessage = error.localizedMessage ?: "No se pudieron cargar los datos de la tarea"
                        )
                    }
                }
            )
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, titleError = null, errorMessage = null) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value, descriptionError = null, errorMessage = null) }
    }

    fun onCategoryChange(value: String) {
        _uiState.update { it.copy(category = value, errorMessage = null) }
    }

    fun onEstimatedTimeChange(value: String) {
        _uiState.update { it.copy(estimatedTimeText = value, estimatedTimeError = null, errorMessage = null) }
    }

    fun onPriorityChange(value: String) {
        _uiState.update { it.copy(priority = value, errorMessage = null) }
    }

    fun onDueDateChange(value: String) {
        _uiState.update {
            it.copy(
                dueDate = value,
                selectedDateMillis = parseDateToUtcMillis(value) ?: it.selectedDateMillis,
                dueDateError = null,
                errorMessage = null
            )
        }
    }

    fun openDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun dismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun onDatePicked(utcMillis: Long?) {
        val formattedDate = utcMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()
        }
        _uiState.update {
            it.copy(
                showDatePicker = false,
                dueDate = formattedDate ?: it.dueDate,
                selectedDateMillis = utcMillis ?: it.selectedDateMillis,
                dueDateError = if (formattedDate != null) null else it.dueDateError,
                errorMessage = null
            )
        }
    }

    fun onCompletedChange(value: Boolean) {
        _uiState.update { it.copy(isCompleted = value, errorMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveTask() {
        val current = _uiState.value
        val titleTrimmed = current.title.trim()
        val descriptionTrimmed = current.description.trim()
        val estimatedTime = current.estimatedTimeText.trim().replace(',', '.').toDoubleOrNull()
        val dueDateTrimmed = current.dueDate.trim()

        val titleError = when {
            titleTrimmed.isEmpty() -> "El título es obligatorio"
            titleTrimmed.length < 3 -> "El título debe tener al menos 3 caracteres"
            titleTrimmed.length > 100 -> "El título no debe exceder 100 caracteres"
            else -> null
        }

        val descriptionError = if (descriptionTrimmed.length > 500) {
            "La descripción no debe exceder 500 caracteres"
        } else {
            null
        }

        val estimatedTimeError = when {
            current.estimatedTimeText.isBlank() -> "El tiempo estimado es obligatorio"
            estimatedTime == null -> "Ingresa un número válido (ej. 1.5)"
            estimatedTime < 0.5 || estimatedTime > 24.0 -> "El tiempo debe estar entre 0.5 y 24.0 horas"
            else -> null
        }

        val dueDateError = when {
            dueDateTrimmed.isEmpty() -> "La fecha límite es obligatoria"
            !ValidationUtils.isValidIsoDate(dueDateTrimmed) -> "Usa el formato YYYY-MM-DD (ej. 2026-11-20)"
            else -> null
        }

        if (titleError != null || descriptionError != null || estimatedTimeError != null || dueDateError != null) {
            _uiState.update {
                it.copy(
                    titleError = titleError,
                    descriptionError = descriptionError,
                    estimatedTimeError = estimatedTimeError,
                    dueDateError = dueDateError
                )
            }
            return
        }

        val domainTask = Task(
            id = taskId ?: 0,
            title = titleTrimmed,
            description = descriptionTrimmed,
            category = current.category,
            estimatedTime = estimatedTime!!,
            priority = current.priority,
            dueDate = dueDateTrimmed,
            isCompleted = current.isCompleted
        )

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = if (taskId != null) {
                repository.updateTask(domainTask)
            } else {
                repository.createTask(domainTask)
            }
            result.fold(
                onSuccess = {
                    val successText = if (taskId != null) {
                        "Tarea actualizada correctamente"
                    } else {
                        "Tarea creada correctamente"
                    }
                    _uiState.update { it.copy(isSaving = false, savedMessage = successText) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.localizedMessage ?: "No se pudo guardar la tarea"
                        )
                    }
                }
            )
        }
    }

    private fun parseDateToUtcMillis(dateText: String): Long? {
        return runCatching {
            LocalDate.parse(dateText.trim()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
    }
}
