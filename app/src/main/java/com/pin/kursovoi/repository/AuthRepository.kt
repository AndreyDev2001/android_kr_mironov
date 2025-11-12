package com.pin.kursovoi.repository

import com.pin.kursovoi.api.RetrofitClient
import com.pin.kursovoi.dto.AuthResponse
import com.pin.kursovoi.dto.LoginRequest
import com.pin.kursovoi.dto.RegisterRequest
import retrofit2.Response

class AuthRepository {
    suspend fun register(request: RegisterRequest): Response<Unit> {
        return RetrofitClient.apiService.register(request)
    }

    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return RetrofitClient.apiService.login(request)
    }
}