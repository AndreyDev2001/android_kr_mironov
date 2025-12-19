package com.pin.kursovoi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel(){
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn


    init {
        _isLoggedIn.value = repository.isLoggedIn()
    }

    fun register(username: String, email: String, password: String) {
        if (!isValidInput(username, email, password)) return

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.register(username, email, password)
            _isLoading.value = false

            result
                .onSuccess { message ->
                    _statusMessage.value = message
                    _isLoggedIn.value = true
                }
                .onFailure { exception ->
                    _statusMessage.value = exception.message
                }
        }
    }

    fun login(usernameOrEmail: String, password: String) {
        if (usernameOrEmail.isBlank() || password.isBlank()) {
            _statusMessage.value = "Введите имя пользователя/email и пароль."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.login(usernameOrEmail, password)
            _isLoading.value = false

            result
                .onSuccess { message ->
                    _statusMessage.value = message
                    _isLoggedIn.value = true
                }
                .onFailure { exception ->
                    _statusMessage.value = exception.message
                }
        }
    }

    fun logout() {
        repository.logout()
        _isLoggedIn.value = false
        _statusMessage.value = "Вы вышли из системы."
    }

    private fun isValidInput(username: String, email: String, password: String): Boolean {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _statusMessage.value = "Все поля обязательны для заполнения."
            return false
        }
        if (password.length < 6) {
            _statusMessage.value = "Пароль должен быть не менее 6 символов."
            return false
        }
        return true
    }
}