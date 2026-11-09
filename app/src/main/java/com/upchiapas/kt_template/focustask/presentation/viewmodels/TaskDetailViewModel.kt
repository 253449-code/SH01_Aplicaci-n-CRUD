package com.upchiapas.kt_template.focustask.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.kt_template.focustask.domain.repository.TaskRepository
import com.upchiapas.kt_template.focustask.presentation.screens.TaskDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository
) : ViewModel() {

    private val taskId: Int = savedStateHandle.get<Int>("taskId") ?: 0

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        loadTask()
    }

    fun loadTask() {
        if (taskId <= 0) {
            _uiState.update { it.copy(errorMessage = "Identificador de tarea inválido") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.getTaskById(taskId)
            result.fold(
                onSuccess = { task ->
                    _uiState.update { it.copy(isLoading = false, task = task) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "No se pudo cargar la tarea"
                        )
                    }
                }
            )
        }
    }

    fun toggleCompletion() {
        val currentTask = _uiState.value.task ?: return
        _uiState.update { it.copy(isUpdating = true) }
        viewModelScope.launch {
            val result = repository.updateTask(currentTask.copy(isCompleted = !currentTask.isCompleted))
            result.fold(
                onSuccess = { updated ->
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            task = updated,
                            userMessage = if (updated.isCompleted) {
                                "Tarea marcada como completada"
                            } else {
                                "Tarea marcada como pendiente"
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            userMessage = error.localizedMessage ?: "No se pudo actualizar el estado"
                        )
                    }
                }
            )
        }
    }

    fun requestDelete() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        val currentTask = _uiState.value.task ?: return
        _uiState.update { it.copy(showDeleteDialog = false, isDeleting = true) }
        viewModelScope.launch {
            val result = repository.deleteTask(currentTask.id)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            deletedSuccessMessage = "Tarea eliminada correctamente"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            userMessage = error.localizedMessage ?: "No se pudo eliminar la tarea"
                        )
                    }
                }
            )
        }
    }

    fun onTaskEdited(message: String?) {
        if (!message.isNullOrBlank()) {
            _uiState.update { it.copy(userMessage = message) }
        }
        loadTask()
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
