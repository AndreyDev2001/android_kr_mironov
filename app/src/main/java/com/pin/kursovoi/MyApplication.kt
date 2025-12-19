package com.pin.kursovoi

import android.app.Application
import android.util.Log

class MyApplication : Application(){
    lateinit var databaseHelper: DatabaseHelper
    lateinit var authRepository: AuthRepository

    companion object {
        private const val TAG = "MyApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate вызвана")
        try {
            databaseHelper = DatabaseHelper(this)
            Log.d(TAG, "DatabaseHelper создан успешно")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при создании DatabaseHelper: ${e.message}", e)
            throw e
        }
        authRepository = AuthRepository(databaseHelper, this)
    }
}