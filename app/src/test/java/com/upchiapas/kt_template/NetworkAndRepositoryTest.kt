package com.upchiapas.kt_template

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.upchiapas.kt_template.auth.data.datasource.AuthApiService
import com.upchiapas.kt_template.auth.data.datasource.AuthInterceptor
import com.upchiapas.kt_template.auth.data.repository.AuthRepositoryImpl
import com.upchiapas.kt_template.auth.domain.model.AuthSession
import com.upchiapas.kt_template.auth.domain.repository.SessionStorage
import com.upchiapas.kt_template.core.network.NetworkErrorMapper
import com.upchiapas.kt_template.focustask.data.datasource.TasksApiService
import com.upchiapas.kt_template.focustask.data.repository.TaskRepositoryImpl
import com.upchiapas.kt_template.focustask.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class NetworkAndRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var sessionStorage: InMemorySessionStorage
    private lateinit var authRepository: AuthRepositoryImpl
    private lateinit var taskRepository: TaskRepositoryImpl

    private var lastAuthHeaderOnTasks: String? = null
    private var lastAuthHeaderOnLogin: String? = null
    private var returnUnauthorizedOnTasks = false

    @Before
    fun setup() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path ?: ""
                val method = request.method ?: ""

                if (path.startsWith("/login")) {
                    lastAuthHeaderOnLogin = request.getHeader("Authorization")
                    val body = """{"access_token":"jwt-abc-123","token_type":"bearer","user":{"id":1,"name":"Emma","email":"emma@test.com"}}"""
                    return MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(body)
                }

                if (path.startsWith("/tasks")) {
                    lastAuthHeaderOnTasks = request.getHeader("Authorization")
                    if (returnUnauthorizedOnTasks) {
                        val err = """{"detail":"Tu sesión ha expirado"}"""
                        return MockResponse()
                            .setResponseCode(401)
                            .setHeader("Content-Type", "application/json")
                            .setBody(err)
                    }

                    val responseJson = when {
                        method == "GET" && path == "/tasks" ->
                            """[{"id":5,"title":"Estudiar","description":"Unit 3","category":"Estudio","estimated_time":2.5,"priority":"Alta","due_date":"2026-11-20","is_completed":false,"created_at":"2026-11-09"}]"""
                        method == "GET" && path == "/tasks/5" ->
                            """{"id":5,"title":"Estudiar","description":"Unit 3","category":"Estudio","estimated_time":2.5,"priority":"Alta","due_date":"2026-11-20","is_completed":false,"created_at":"2026-11-09"}"""
                        method == "POST" && path == "/tasks" ->
                            """{"id":6,"title":"Nueva","description":"Desc","category":"Trabajo","estimated_time":1.0,"priority":"Media","due_date":"2026-11-21","is_completed":false,"created_at":"2026-11-09"}"""
                        method == "PUT" && path == "/tasks/6" ->
                            """{"id":6,"title":"Editada","description":"Desc","category":"Trabajo","estimated_time":1.5,"priority":"Baja","due_date":"2026-11-22","is_completed":true,"created_at":"2026-11-09"}"""
                        method == "DELETE" && path == "/tasks/6" ->
                            """{"message":"Tarea eliminada"}"""
                        else -> "{}"
                    }
                    return MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(responseJson)
                }

                return MockResponse().setResponseCode(404)
            }
        }
        server.start()

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
        sessionStorage = InMemorySessionStorage()
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionStorage))
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val errorMapper = NetworkErrorMapper(json)
        authRepository = AuthRepositoryImpl(
            apiService = retrofit.create(AuthApiService::class.java),
            sessionStorage = sessionStorage,
            errorMapper = errorMapper
        )
        taskRepository = TaskRepositoryImpl(
            apiService = retrofit.create(TasksApiService::class.java),
            errorMapper = errorMapper
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun login_savesToken_interceptorAttachesBearer_andCrudWorksEndToEnd() = runTest {
        val loginResult = authRepository.login("emma@test.com", "Password123")
        assertTrue(loginResult.isSuccess)
        assertNull(lastAuthHeaderOnLogin)
        assertEquals("jwt-abc-123", sessionStorage.getToken())

        val tasksResult = taskRepository.getTasks()
        assertTrue(tasksResult.isSuccess)
        assertEquals("Bearer jwt-abc-123", lastAuthHeaderOnTasks)
        assertEquals(1, tasksResult.getOrThrow().size)
        assertEquals(2.5, tasksResult.getOrThrow().first().estimatedTime, 0.001)

        val detailResult = taskRepository.getTaskById(5)
        assertTrue(detailResult.isSuccess)
        assertEquals("Estudiar", detailResult.getOrThrow().title)

        val created = taskRepository.createTask(
            Task(
                title = "Nueva",
                description = "Desc",
                category = "Trabajo",
                estimatedTime = 1.0,
                priority = "Media",
                dueDate = "2026-11-21"
            )
        )
        assertTrue(created.isSuccess)
        assertEquals(6, created.getOrThrow().id)

        val updated = taskRepository.updateTask(
            created.getOrThrow().copy(title = "Editada", isCompleted = true)
        )
        assertTrue(updated.isSuccess)
        assertTrue(updated.getOrThrow().isCompleted)

        val deleted = taskRepository.deleteTask(6)
        assertTrue(deleted.isSuccess)
    }

    @Test
    fun unauthorizedResponse_clearsSessionAndMapsDetailMessage() = runTest {
        authRepository.login("emma@test.com", "Password123")
        assertEquals("jwt-abc-123", sessionStorage.getToken())

        returnUnauthorizedOnTasks = true
        val result = taskRepository.getTasks()
        assertTrue(result.isFailure)
        assertEquals("Tu sesión ha expirado", result.exceptionOrNull()?.message)
        assertNull(sessionStorage.getToken())
    }
}

private class InMemorySessionStorage : SessionStorage {
    private val _session = MutableStateFlow<AuthSession?>(null)
    override val sessionFlow: Flow<AuthSession?> = _session

    override suspend fun getToken(): String? = _session.value?.token

    override suspend fun saveSession(session: AuthSession) {
        _session.value = session
    }

    override suspend fun clearSession() {
        _session.value = null
    }
}
