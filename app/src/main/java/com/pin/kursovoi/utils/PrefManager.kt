package com.pin.kursovoi.utils

import android.content.Context
import android.content.SharedPreferences

class PrefManager (context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ElectronicsStorePrefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    fun clearToken() {
        prefs.edit().remove("token").apply()
    }
}