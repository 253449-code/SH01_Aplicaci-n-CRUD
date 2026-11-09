package com.upchiapas.kt_template.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.upchiapas.kt_template.KtApplication
import com.upchiapas.kt_template.auth.presentation.screens.LoginPage
import com.upchiapas.kt_template.auth.presentation.screens.RegisterPage
import com.upchiapas.kt_template.auth.presentation.screens.SessionUiState
import com.upchiapas.kt_template.auth.presentation.viewmodels.SessionViewModel
import com.upchiapas.kt_template.focustask.presentation.screens.TaskDetailPage
import com.upchiapas.kt_template.focustask.presentation.screens.TaskFormPage
import com.upchiapas.kt_template.focustask.presentation.screens.TasksHomePage

@Composable
fun AppNavGraph(
    sessionViewModel: SessionViewModel = viewModel(
        factory = (LocalContext.current.applicationContext as KtApplication).appContainer.sessionViewModelFactory
    )
) {
    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    var initialDestination by rememberSaveable { mutableStateOf<String?>(null) }

    if (initialDestination == null && sessionState !is SessionUiState.Loading) {
        initialDestination = if (sessionState is SessionUiState.Authenticated) {
            AppRoutes.HOME
        } else {
            AppRoutes.LOGIN
        }
    }

    val startDestination = initialDestination
    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()

    LaunchedEffect(sessionState) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        when (sessionState) {
            is SessionUiState.Authenticated -> {
                if (currentRoute == AppRoutes.LOGIN || currentRoute == AppRoutes.REGISTER) {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            is SessionUiState.Unauthenticated -> {
                if (currentRoute != null &&
                    currentRoute != AppRoutes.LOGIN &&
                    currentRoute != AppRoutes.REGISTER
                ) {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            SessionUiState.Loading -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = AppRoutes.LOGIN) {
            LoginPage(
                onNavigateToRegister = {
                    navController.navigate(AppRoutes.REGISTER)
                }
            )
        }

        composable(route = AppRoutes.REGISTER) {
            RegisterPage(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = AppRoutes.HOME) { backStackEntry ->
            val refreshSignal by backStackEntry.savedStateHandle
                .getStateFlow<String?>(AppRoutes.KEY_OPERATION_MESSAGE, null)
                .collectAsStateWithLifecycle()

            TasksHomePage(
                refreshSignal = refreshSignal,
                onRefreshSignalConsumed = {
                    backStackEntry.savedStateHandle.remove<String>(AppRoutes.KEY_OPERATION_MESSAGE)
                },
                onNavigateToDetail = { taskId ->
                    navController.navigate(AppRoutes.taskDetail(taskId))
                },
                onNavigateToCreate = {
                    navController.navigate(AppRoutes.taskCreate())
                },
                onNavigateToEdit = { taskId ->
                    navController.navigate(AppRoutes.taskEdit(taskId))
                }
            )
        }

        composable(
            route = AppRoutes.TASK_DETAIL,
            arguments = listOf(
                navArgument(AppRoutes.ARG_TASK_ID) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val refreshSignal by backStackEntry.savedStateHandle
                .getStateFlow<String?>(AppRoutes.KEY_OPERATION_MESSAGE, null)
                .collectAsStateWithLifecycle()

            TaskDetailPage(
                refreshSignal = refreshSignal,
                onRefreshSignalConsumed = {
                    backStackEntry.savedStateHandle.remove<String>(AppRoutes.KEY_OPERATION_MESSAGE)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { taskId ->
                    navController.navigate(AppRoutes.taskEdit(taskId))
                },
                onTaskDeleted = { message ->
                    runCatching { navController.getBackStackEntry(AppRoutes.HOME) }
                        .getOrNull()
                        ?.savedStateHandle
                        ?.set(AppRoutes.KEY_OPERATION_MESSAGE, message)
                    navController.popBackStack()
                },
                onTaskMutated = {
                    runCatching { navController.getBackStackEntry(AppRoutes.HOME) }
                        .getOrNull()
                        ?.savedStateHandle
                        ?.set(AppRoutes.KEY_OPERATION_MESSAGE, "")
                }
            )
        }

        composable(
            route = AppRoutes.TASK_FORM,
            arguments = listOf(
                navArgument(AppRoutes.ARG_TASK_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            TaskFormPage(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSavedSuccess = { message ->
                    runCatching { navController.getBackStackEntry(AppRoutes.HOME) }
                        .getOrNull()
                        ?.savedStateHandle
                        ?.set(AppRoutes.KEY_OPERATION_MESSAGE, message)

                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(AppRoutes.KEY_OPERATION_MESSAGE, message)

                    navController.popBackStack()
                }
            )
        }
    }
}
