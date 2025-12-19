package com.pin.kursovoi

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest


class AuthRepository(private val dbHelper: DatabaseHelper, private val context: Context) {
    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_ID_KEY = "user_id"
        const val USERNAME_KEY = "username"
        const val IS_LOGGED_IN_KEY = "is_logged_in"
    }

    suspend fun register(username: String, email: String, password: String): Result<String> {
        // 1. Проверка уникальности имени пользователя и email
        if (dbHelper.getUserByUsername(username) != null) {
            return Result.failure(Exception("Пользователь с таким именем уже существует."))
        }
        if (dbHelper.getUserByEmail(email) != null) {
            return Result.failure(Exception("Пользователь с таким email уже существует."))
        }

        // 2. Хеширование пароля
        val hashedPassword = hashPassword(password)

        // 3. Создание объекта User
        val newUser = User(
            userId = 0, // будет установлен базой данных
            username = username,
            email = email,
            passwordHash = hashedPassword
        )

        // 4. Сохранение в БД
        try {
            val userId = dbHelper.insertUser(newUser)
            if (userId != -1L) { // -1 означает ошибку при вставке
                saveLoggedInUser(userId, username)
                return Result.success("Регистрация успешна.")
            } else {
                return Result.failure(Exception("Ошибка при регистрации."))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun login(usernameOrEmail: String, password: String): Result<String> {
        // 1. Ищем пользователя по имени или email
        val user = dbHelper.getUserByUsername(usernameOrEmail)
            ?: dbHelper.getUserByEmail(usernameOrEmail)

        // 2. Если пользователь не найден
        if (user == null) {
            return Result.failure(Exception("Пользователь не найден."))
        }

        // 3. Проверка пароля
        val hashedInputPassword = hashPassword(password)
        if (hashedInputPassword != user.passwordHash) {
            return Result.failure(Exception("Неверный пароль."))
        }

        // 4. Если всё верно - сохраняем сессию
        saveLoggedInUser(user.userId, user.username)
        return Result.success("Вход выполнен успешно.")
    }

    fun logout() {
        with(sharedPreferences.edit()) {
            remove(USER_ID_KEY)
            remove(USERNAME_KEY)
            putBoolean(IS_LOGGED_IN_KEY, false)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(IS_LOGGED_IN_KEY, false)
    }

    fun getCurrentUsername(): String? {
        return if (isLoggedIn()) {
            sharedPreferences.getString(USERNAME_KEY, null)
        } else {
            null
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun saveLoggedInUser(userId: Long, username: String) {
        with(sharedPreferences.edit()) {
            putLong(USER_ID_KEY, userId)
            putString(USERNAME_KEY, username)
            putBoolean(IS_LOGGED_IN_KEY, true)
            apply()
        }
    }
}