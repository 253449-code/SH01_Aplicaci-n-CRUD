package com.upchiapas.kt_template.focustask.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.kt_template.KtApplication
import com.upchiapas.kt_template.focustask.domain.model.Task
import com.upchiapas.kt_template.focustask.presentation.viewmodels.TaskFormViewModel

@Composable
fun TaskFormPage(
    onNavigateBack: () -> Unit,
    onSavedSuccess: (String) -> Unit,
    viewModel: TaskFormViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as KtApplication).appContainer.taskFormViewModelFactory
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedMessage) {
        uiState.savedMessage?.let { message ->
            onSavedSuccess(message)
        }
    }

    TaskFormContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetryLoad = viewModel::retryLoad,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onCategoryChange = viewModel::onCategoryChange,
        onEstimatedTimeChange = viewModel::onEstimatedTimeChange,
        onPriorityChange = viewModel::onPriorityChange,
        onDueDateChange = viewModel::onDueDateChange,
        onOpenDatePicker = viewModel::openDatePicker,
        onDismissDatePicker = viewModel::dismissDatePicker,
        onDatePicked = viewModel::onDatePicked,
        onCompletedChange = viewModel::onCompletedChange,
        onSaveClick = viewModel::saveTask,
        onClearErrorMessage = viewModel::clearErrorMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormContent(
    uiState: TaskFormUiState,
    onNavigateBack: () -> Unit,
    onRetryLoad: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onEstimatedTimeChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onDueDateChange: (String) -> Unit,
    onOpenDatePicker: () -> Unit,
    onDismissDatePicker: () -> Unit,
    onDatePicked: (Long?) -> Unit,
    onCompletedChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onClearErrorMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearErrorMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "Editar tarea" else "Nueva tarea")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoadingInitialData -> {
                    CircularProgressIndicator()
                }

                uiState.loadErrorMessage != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = uiState.loadErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetryLoad) {
                            Text("Reintentar")
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (uiState.errorMessage != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = uiState.errorMessage,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = onTitleChange,
                            label = { Text("Título *") },
                            placeholder = { Text("Ej. Estudiar Retrofit y MVVM") },
                            isError = uiState.titleError != null,
                            supportingText = uiState.titleError?.let { error -> { Text(error) } },
                            singleLine = true,
                            enabled = !uiState.isSaving,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = onDescriptionChange,
                            label = { Text("Descripción") },
                            placeholder = { Text("Detalles o notas de la tarea") },
                            isError = uiState.descriptionError != null,
                            supportingText = uiState.descriptionError?.let { error -> { Text(error) } },
                            minLines = 3,
                            maxLines = 5,
                            enabled = !uiState.isSaving,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Categoría",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Task.CATEGORIES.forEach { category ->
                                    FilterChip(
                                        selected = uiState.category == category,
                                        onClick = { onCategoryChange(category) },
                                        enabled = !uiState.isSaving,
                                        label = { Text(category) }
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = uiState.estimatedTimeText,
                                onValueChange = onEstimatedTimeChange,
                                label = { Text("Tiempo estimado (horas) *") },
                                placeholder = { Text("0.5 a 24.0") },
                                isError = uiState.estimatedTimeError != null,
                                supportingText = uiState.estimatedTimeError?.let { error -> { Text(error) } },
                                singleLine = true,
                                enabled = !uiState.isSaving,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf("0.5", "1.0", "2.0", "4.0").forEach { preset ->
                                    FilterChip(
                                        selected = uiState.estimatedTimeText == preset,
                                        onClick = { onEstimatedTimeChange(preset) },
                                        enabled = !uiState.isSaving,
                                        label = { Text("${preset}h") }
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Prioridad",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Task.PRIORITIES.forEach { priority ->
                                    FilterChip(
                                        selected = uiState.priority == priority,
                                        onClick = { onPriorityChange(priority) },
                                        enabled = !uiState.isSaving,
                                        label = { Text(priority) }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = uiState.dueDate,
                            onValueChange = onDueDateChange,
                            label = { Text("Fecha límite (YYYY-MM-DD) *") },
                            placeholder = { Text("2026-11-20") },
                            trailingIcon = {
                                IconButton(
                                    onClick = onOpenDatePicker,
                                    enabled = !uiState.isSaving
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Seleccionar fecha"
                                    )
                                }
                            },
                            isError = uiState.dueDateError != null,
                            supportingText = uiState.dueDateError?.let { error -> { Text(error) } },
                            singleLine = true,
                            enabled = !uiState.isSaving,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Marcar como completada",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = uiState.isCompleted,
                                onCheckedChange = onCompletedChange,
                                enabled = !uiState.isSaving
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = onSaveClick,
                            enabled = !uiState.isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(if (uiState.isEditMode) "Guardar cambios" else "Crear tarea")
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.selectedDateMillis
            )
            DatePickerDialog(
                onDismissRequest = onDismissDatePicker,
                confirmButton = {
                    TextButton(
                        onClick = { onDatePicked(datePickerState.selectedDateMillis) }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDatePicker) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
