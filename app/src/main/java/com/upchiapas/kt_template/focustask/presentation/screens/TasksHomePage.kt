package com.upchiapas.kt_template.focustask.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.kt_template.KtApplication
import com.upchiapas.kt_template.focustask.domain.model.Task
import com.upchiapas.kt_template.focustask.presentation.components.DeleteConfirmationDialog
import com.upchiapas.kt_template.focustask.presentation.components.ProgressSection
import com.upchiapas.kt_template.focustask.presentation.components.TaskCard
import com.upchiapas.kt_template.focustask.presentation.viewmodels.TasksHomeViewModel

@Composable
fun TasksHomePage(
    refreshSignal: String?,
    onRefreshSignalConsumed: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    viewModel: TasksHomeViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as KtApplication).appContainer.tasksHomeViewModelFactory
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(refreshSignal) {
        if (refreshSignal != null) {
            viewModel.onExternalOperationCompleted(refreshSignal)
            onRefreshSignalConsumed()
        }
    }

    TasksHomeContent(
        uiState = uiState,
        onRefresh = { viewModel.fetchTasks() },
        onToggleShowCompleted = viewModel::toggleShowCompleted,
        onTaskClick = onNavigateToDetail,
        onToggleTaskCompletion = viewModel::toggleTaskCompletion,
        onCreateClick = onNavigateToCreate,
        onEditClick = onNavigateToEdit,
        onRequestDelete = viewModel::requestDeleteTask,
        onConfirmDelete = viewModel::confirmDeleteTask,
        onDismissDelete = viewModel::dismissDeleteDialog,
        onLogoutClick = viewModel::logout,
        onClearUserMessage = viewModel::clearUserMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksHomeContent(
    uiState: TasksHomeUiState,
    onRefresh: () -> Unit,
    onToggleShowCompleted: (Boolean) -> Unit,
    onTaskClick: (Int) -> Unit,
    onToggleTaskCompletion: (Task) -> Unit,
    onCreateClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onRequestDelete: (Task) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onLogoutClick: () -> Unit,
    onClearUserMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "FocusTask")
                        if (uiState.userName.isNotBlank()) {
                            Text(
                                text = "Hola, ${uiState.userName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar lista"
                        )
                    }
                    TextButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Salir")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear tarea"
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isRefreshing || uiState.isDeleting) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Cargando tareas...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    uiState.errorMessage != null -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRefresh) {
                                Text("Reintentar")
                            }
                        }
                    }

                    uiState.tasks.isEmpty() -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Aún no tienes tareas registradas",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Presiona el botón + para crear tu primera tarea.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = onCreateClick) {
                                Text("Crear tarea")
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item(key = "progress_header") {
                                ProgressSection(
                                    completedCount = uiState.completedCount,
                                    totalCount = uiState.totalCount,
                                    totalPoints = uiState.totalPoints,
                                    showCompleted = uiState.showCompleted,
                                    onToggleShowCompleted = onToggleShowCompleted
                                )
                            }

                            if (uiState.visibleTasks.isEmpty()) {
                                item(key = "filtered_empty") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No hay tareas pendientes por mostrar.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                items(uiState.visibleTasks, key = { it.id }) { task ->
                                    TaskCard(
                                        task = task,
                                        onClick = { onTaskClick(task.id) },
                                        onToggleComplete = { onToggleTaskCompletion(task) },
                                        onEditClick = { onEditClick(task.id) },
                                        onDeleteClick = { onRequestDelete(task) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        uiState.taskToDelete?.let { task ->
            DeleteConfirmationDialog(
                taskTitle = task.title,
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete
            )
        }
    }
}
