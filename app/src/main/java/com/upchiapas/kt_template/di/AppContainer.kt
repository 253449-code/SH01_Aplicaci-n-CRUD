package com.upchiapas.kt_template.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.upchiapas.kt_template.BuildConfig
import com.upchiapas.kt_template.auth.data.datasource.AuthApiService
import com.upchiapas.kt_template.auth.data.datasource.AuthInterceptor
import com.upchiapas.kt_template.auth.data.datasource.DataStoreSessionStorage
import com.upchiapas.kt_template.auth.data.datasource.sessionDataStore
import com.upchiapas.kt_template.auth.data.repository.AuthRepositoryImpl
import com.upchiapas.kt_template.auth.domain.repository.AuthRepository
import com.upchiapas.kt_template.auth.domain.repository.SessionStorage
import com.upchiapas.kt_template.auth.presentation.viewmodels.LoginViewModel
import com.upchiapas.kt_template.auth.presentation.viewmodels.RegisterViewModel
import com.upchiapas.kt_template.auth.presentation.viewmodels.SessionViewModel
import com.upchiapas.kt_template.core.network.ErrorMapper
import com.upchiapas.kt_template.core.network.NetworkErrorMapper
import com.upchiapas.kt_template.focustask.data.datasource.TasksApiService
import com.upchiapas.kt_template.focustask.data.repository.TaskRepositoryImpl
import com.upchiapas.kt_template.focustask.domain.repository.TaskRepository
import com.upchiapas.kt_template.focustask.presentation.viewmodels.TaskDetailViewModel
import com.upchiapas.kt_template.focustask.presentation.viewmodels.TaskFormViewModel
import com.upchiapas.kt_template.focustask.presentation.viewmodels.TasksHomeViewModel
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val sessionStorage: SessionStorage
    val authRepository: AuthRepository
    val taskRepository: TaskRepository
    val sessionViewModelFactory: ViewModelProvider.Factory
    val loginViewModelFactory: ViewModelProvider.Factory
    val registerViewModelFactory: ViewModelProvider.Factory
    val tasksHomeViewModelFactory: ViewModelProvider.Factory
    val taskDetailViewModelFactory: ViewModelProvider.Factory
    val taskFormViewModelFactory: ViewModelProvider.Factory
}

class DefaultAppContainer(
    private val context: Context,
    private val baseUrl: String = BuildConfig.API_BASE_URL
) : AppContainer {

    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    private val errorMapper: ErrorMapper by lazy {
        NetworkErrorMapper(json)
    }

    override val sessionStorage: SessionStorage by lazy {
        DataStoreSessionStorage(context.applicationContext.sessionDataStore)
    }

    private val authInterceptor: Interceptor by lazy {
        AuthInterceptor(sessionStorage)
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    private val tasksApiService: TasksApiService by lazy {
        retrofit.create(TasksApiService::class.java)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            apiService = authApiService,
            sessionStorage = sessionStorage,
            errorMapper = errorMapper
        )
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(
            apiService = tasksApiService,
            errorMapper = errorMapper
        )
    }

    override val sessionViewModelFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer {
                SessionViewModel(authRepository)
            }
        }
    }

    override val loginViewModelFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer {
                LoginViewModel(authRepository)
            }
        }
    }

    override val registerViewModelFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer {
                RegisterViewModel(authRepository)
            }
        }
    }

    override val tasksHomeViewModelFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer {
                TasksHomeViewModel(
                    taskRepository = taskRepository,
                    authRepository = authRepository
                )
            }
        }
    }

    override val taskDetailViewModelFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer {
                TaskDetailViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = taskRepository
                )
            }
        }
    }

    override val taskFormViewModelFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer {
                TaskFormViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    repository = taskRepository
                )
            }
        }
    }
}
