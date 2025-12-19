package com.pin.kursovoi

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var viewModel: AuthViewModel

    @Mock
    private lateinit var mockAuthRepository: AuthRepository

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(mockAuthRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login updates status message and isLoggedIn on success`() = runTest {
        val username = "ivan"
        val password = "ivan1234"
        val successMessage = "Вход выполнен успешно."
        `when`(mockAuthRepository.login(username, password)).thenReturn(Result.success(successMessage))

        viewModel.login(username, password)

        // Пропускаем задачи корутин, чтобы LiveData обновились
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(successMessage, viewModel.statusMessage.value)
        assertTrue { viewModel.isLoggedIn.value == true }
    }

    @Test
    fun `login updates status message on failure`() = runTest {
        val username = "testuser"
        val password = "wrongpassword"
        val errorMessage = "Неверный пароль."
        `when`(mockAuthRepository.login(username, password)).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.login(username, password)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(errorMessage, viewModel.statusMessage.value)
    }

    @Test
    fun `register updates status message and isLoggedIn on success`() = runTest {
        val username = "user"
        val email = "user@mailr.ru"
        val password = "password123"
        val successMessage = "Регистрация успешна."
        `when`(mockAuthRepository.register(username, email, password)).thenReturn(Result.success(successMessage))

        viewModel.register(username, email, password)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(successMessage, viewModel.statusMessage.value)
        assertTrue { viewModel.isLoggedIn.value == true }
    }

    @Test
    fun `register updates status message on failure due to validation`() = runTest {
        val username = "petr"
        val email = "petr@mail.ru"
        val password = "short" // Нарушает isValidInput (меньше 6 символов)

        // Вызов register с коротким паролем
        viewModel.register(username, email, password)

        testDispatcher.scheduler.advanceUntilIdle()

        // Проверим, что repository не был вызван из-за проверки валидации в ViewModel
        verify(mockAuthRepository, never()).register(any(), any(), any())
        // И statusMessage должен содержать сообщение об ошибке ввода
        assertTrue { viewModel.statusMessage.value?.contains("Пароль должен быть не менее 6 символов") == true }
    }

    @Test
    fun `register updates status message on failure from repository`() = runTest {
        val username = "ivan"
        val email = "ivan@mail.ru"
        val password = "ivan1234"
        val errorMessage = "Пользователь с таким именем уже существует."
        `when`(mockAuthRepository.register(username, email, password)).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.register(username, email, password)

        testDispatcher.scheduler.advanceUntilIdle()

        // repository вызван, но вернул ошибку
        verify(mockAuthRepository).register(username, email, password)
        assertEquals(errorMessage, viewModel.statusMessage.value)
    }

    @Test
    fun `logout calls repository logout and updates state`() {
        viewModel.logout()

        verify(mockAuthRepository).logout()
        assertFalse { viewModel.isLoggedIn.value == true }
        assertTrue { viewModel.statusMessage.value?.contains("вышли из системы") == true }
    }
}