package com.upchiapas.kt_template

import com.upchiapas.kt_template.auth.domain.model.AuthSession
import com.upchiapas.kt_template.auth.domain.model.User
import com.upchiapas.kt_template.auth.domain.repository.AuthRepository
import com.upchiapas.kt_template.auth.presentation.viewmodels.LoginViewModel
import com.upchiapas.kt_template.auth.presentation.viewmodels.RegisterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
class AuthViewModelsTest {

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
    fun loginViewModel_validatesEmptyAndInvalidFields() = runTest {
        val repo = FakeAuthRepository()
        val vm = LoginViewModel(repo)

        vm.login()
        assertNotNull(vm.uiState.value.emailError)
        assertNotNull(vm.uiState.value.passwordError)
        assertEquals(0, repo.loginCalls)

        vm.onEmailChange("correo-invalido")
        vm.onPasswordChange("123")
        vm.login()
        assertEquals("Ingresa un correo electrónico válido", vm.uiState.value.emailError)
        assertEquals("La contraseña debe tener al menos 8 caracteres", vm.uiState.value.passwordError)
        assertEquals(0, repo.loginCalls)
    }

    @Test
    fun loginViewModel_submitsValidCredentialsAndHandlesError() = runTest {
        val repo = FakeAuthRepository(shouldFail = true)
        val vm = LoginViewModel(repo)

        vm.onEmailChange("emma@example.com")
        vm.onPasswordChange("Password123")
        vm.login()
        advanceUntilIdle()

        assertEquals(1, repo.loginCalls)
        assertFalse(vm.uiState.value.isLoading)
        assertEquals("Correo o contraseña incorrectos", vm.uiState.value.errorMessage)
    }

    @Test
    fun registerViewModel_validatesNameEmailPasswordAndConfirmation() = runTest {
        val repo = FakeAuthRepository()
        val vm = RegisterViewModel(repo)

        vm.onNameChange("A")
        vm.onEmailChange("bad")
        vm.onPasswordChange("1234567")
        vm.onConfirmPasswordChange("different")
        vm.register()

        assertNotNull(vm.uiState.value.nameError)
        assertNotNull(vm.uiState.value.emailError)
        assertNotNull(vm.uiState.value.passwordError)
        assertEquals("Las contraseñas no coinciden", vm.uiState.value.confirmPasswordError)
        assertEquals(0, repo.registerCalls)

        vm.onNameChange("Emma López")
        vm.onEmailChange("emma@example.com")
        vm.onPasswordChange("Password123")
        vm.onConfirmPasswordChange("Password123")
        vm.register()
        advanceUntilIdle()

        assertEquals(1, repo.registerCalls)
        assertNull(vm.uiState.value.errorMessage)
        assertTrue(repo.currentSession != null)
    }
}

class FakeAuthRepository(
    private val shouldFail: Boolean = false
) : AuthRepository {
    private val _sessionFlow = MutableStateFlow<AuthSession?>(null)
    override val sessionFlow: Flow<AuthSession?> = _sessionFlow

    var loginCalls = 0
    var registerCalls = 0
    var logoutCalls = 0
    val currentSession: AuthSession? get() = _sessionFlow.value

    override suspend fun register(name: String, email: String, password: String): Result<AuthSession> {
        registerCalls++
        if (shouldFail) return Result.failure(Exception("Ya existe una cuenta registrada"))
        val session = AuthSession(token = "fake-jwt", user = User(1, name, email))
        _sessionFlow.value = session
        return Result.success(session)
    }

    override suspend fun login(email: String, password: String): Result<AuthSession> {
        loginCalls++
        if (shouldFail) return Result.failure(Exception("Correo o contraseña incorrectos"))
        val session = AuthSession(token = "fake-jwt", user = User(1, "Usuario", email))
        _sessionFlow.value = session
        return Result.success(session)
    }

    override suspend fun logout() {
        logoutCalls++
        _sessionFlow.value = null
    }
}
