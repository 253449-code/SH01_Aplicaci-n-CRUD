package com.upchiapas.kt_template

import androidx.lifecycle.SavedStateHandle
import com.upchiapas.kt_template.focustask.domain.model.Task
import com.upchiapas.kt_template.focustask.domain.repository.TaskRepository
import com.upchiapas.kt_template.focustask.presentation.viewmodels.TaskFormViewModel
import com.upchiapas.kt_template.focustask.presentation.viewmodels.TasksHomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelsTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun taskFormViewModel_validatesFieldsBeforeCreating() = runTest {
        val repo = FakeTaskRepository()
        val vm = TaskFormViewModel(SavedStateHandle(), repo)

        vm.onTitleChange("ab")
        vm.onEstimatedTimeChange("99")
        vm.onDueDateChange("invalid-date")
        vm.saveTask()

        assertNotNull(vm.uiState.value.titleError)
        assertNotNull(vm.uiState.value.estimatedTimeError)
        assertNotNull(vm.uiState.value.dueDateError)
        assertEquals(0, repo.createCalls)

        vm.onTitleChange("Preparar exposición")
        vm.onDescriptionChange("Diapositivas de Clean Architecture")
        vm.onEstimatedTimeChange("2.5")
        vm.onDueDateChange("2026-11-25")
        vm.saveTask()
        advanceUntilIdle()

        assertEquals(1, repo.createCalls)
        assertEquals("Tarea creada correctamente", vm.uiState.value.savedMessage)
    }

    @Test
    fun taskFormViewModel_preloadsAndUpdatesInEditMode() = runTest {
        val existing = Task(
            id = 7,
            title = "Leer PDF",
            description = "Rúbrica unidad 3",
            category = "Estudio",
            estimatedTime = 1.5,
            priority = "Alta",
            dueDate = "2026-11-18",
            isCompleted = false
        )
        val repo = FakeTaskRepository(initialTasks = listOf(existing))
        val vm = TaskFormViewModel(SavedStateHandle(mapOf("taskId" to 7)), repo)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isEditMode)
        assertEquals("Leer PDF", vm.uiState.value.title)
        assertEquals("1.5", vm.uiState.value.estimatedTimeText)

        vm.onTitleChange("Leer PDF completo")
        vm.onCompletedChange(true)
        vm.saveTask()
        advanceUntilIdle()

        assertEquals(1, repo.updateCalls)
        assertEquals("Tarea actualizada correctamente", vm.uiState.value.savedMessage)
    }

    @Test
    fun tasksHomeViewModel_crudFilterAndLogoutWork() = runTest {
        val task = Task(
            id = 1,
            title = "Tarea inicial",
            description = "Descripción",
            category = "Trabajo",
            estimatedTime = 2.0,
            priority = "Media",
            dueDate = "2026-11-20",
            isCompleted = false
        )
        val taskRepo = FakeTaskRepository(initialTasks = listOf(task))
        val authRepo = FakeAuthRepository()
        val vm = TasksHomeViewModel(taskRepo, authRepo)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.tasks.size)
        assertFalse(vm.uiState.value.isLoading)

        vm.toggleTaskCompletion(task)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.tasks.first().isCompleted)

        vm.toggleShowCompleted(false)
        assertTrue(vm.uiState.value.visibleTasks.isEmpty())

        vm.requestDeleteTask(vm.uiState.value.tasks.first())
        assertNotNull(vm.uiState.value.taskToDelete)
        vm.confirmDeleteTask()
        advanceUntilIdle()
        assertNull(vm.uiState.value.taskToDelete)
        assertTrue(vm.uiState.value.tasks.isEmpty())

        vm.logout()
        advanceUntilIdle()
        assertEquals(1, authRepo.logoutCalls)
    }
}

class FakeTaskRepository(
    initialTasks: List<Task> = emptyList()
) : TaskRepository {
    private val items = initialTasks.toMutableList()
    var createCalls = 0
    var updateCalls = 0

    override suspend fun getTasks(): Result<List<Task>> = Result.success(items.toList())

    override suspend fun getTaskById(id: Int): Result<Task> {
        val found = items.find { it.id == id }
        return if (found != null) Result.success(found) else Result.failure(Exception("No encontrada"))
    }

    override suspend fun createTask(task: Task): Result<Task> {
        createCalls++
        val created = task.copy(id = (items.maxOfOrNull { it.id } ?: 0) + 1)
        items.add(0, created)
        return Result.success(created)
    }

    override suspend fun updateTask(task: Task): Result<Task> {
        updateCalls++
        val index = items.indexOfFirst { it.id == task.id }
        if (index == -1) return Result.failure(Exception("No encontrada"))
        items[index] = task
        return Result.success(task)
    }

    override suspend fun deleteTask(id: Int): Result<Unit> {
        items.removeAll { it.id == id }
        return Result.success(Unit)
    }
}
