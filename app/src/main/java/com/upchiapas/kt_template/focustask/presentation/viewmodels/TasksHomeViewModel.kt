package com.upchiapas.kt_template.focustask.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.kt_template.auth.domain.repository.AuthRepository
import com.upchiapas.kt_template.focustask.domain.model.Task
import com.upchiapas.kt_template.focustask.domain.repository.TaskRepository
import com.upchiapas.kt_template.focustask.presentation.screens.TasksHomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksHomeViewModel(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksHomeUiState())
    val uiState: StateFlow<TasksHomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionFlow.collectLatest { session ->
                _uiState.update { it.copy(userName = session?.user?.name.orEmpty()) }
            }
        }
        fetchTasks()
    }

    fun fetchTasks(silent: Boolean = false) {
        _uiState.update {
            if (silent) {
                it.copy(isRefreshing = true, errorMessage = null)
            } else {
                it.copy(isLoading = true, errorMessage = null)
            }
        }
        viewModelScope.launch {
            val result = taskRepository.getTasks()
            result.fold(
                onSuccess = { tasks ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            tasks = tasks,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    val message = error.localizedMessage ?: "Error al cargar las tareas"
                    _uiState.update { state ->
                        if (state.tasks.isEmpty()) {
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = message
                            )
                        } else {
                            state.copy(
                                isLoading = false,
                                isRefreshing = false,
                                userMessage = message
                            )
                        }
                    }
                }
            )
        }
    }

    fun toggleShowCompleted(show: Boolean) {
        _uiState.update { it.copy(showCompleted = show) }
    }

    fun toggleTaskCompletion(task: Task) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        viewModelScope.launch {
            val result = taskRepository.updateTask(updated)
            result.fold(
                onSuccess = { savedTask ->
                    val feedback = if (savedTask.isCompleted) {
                        "Tarea completada (+${savedTask.points} pts)"
                    } else {
                        "Tarea marcada como pendiente"
                    }
                    _uiState.update { state ->
                        state.copy(
                            tasks = state.tasks.map { if (it.id == savedTask.id) savedTask else it },
                            userMessage = feedback
                        )
                    }
                    fetchTasks(silent = true)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(userMessage = error.localizedMessage ?: "Error al actualizar la tarea")
                    }
                }
            )
        }
    }

    fun requestDeleteTask(task: Task) {
        _uiState.update { it.copy(taskToDelete = task) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(taskToDelete = null) }
    }

    fun confirmDeleteTask() {
        val task = _uiState.value.taskToDelete ?: return
        _uiState.update { it.copy(taskToDelete = null, isDeleting = true) }
        viewModelScope.launch {
            val result = taskRepository.deleteTask(task.id)
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            isDeleting = false,
                            tasks = state.tasks.filterNot { it.id == task.id },
                            userMessage = "Tarea eliminada correctamente"
                        )
                    }
                    fetchTasks(silent = true)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            userMessage = error.localizedMessage ?: "Error al eliminar la tarea"
                        )
                    }
                }
            )
        }
    }

    fun onExternalOperationCompleted(message: String?) {
        if (!message.isNullOrBlank()) {
            _uiState.update { it.copy(userMessage = message) }
        }
        fetchTasks(silent = _uiState.value.tasks.isNotEmpty())
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
