package com.pin.kursovoi.api

import com.pin.kursovoi.dto.AuthResponse
import com.pin.kursovoi.dto.LoginRequest
import com.pin.kursovoi.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}