package com.pin.kursovoi

import java.util.Date

data class Review(
    val reviewId: Long,
    val productId: Long,
    val userId: Long, // ID пользователя, оставившего отзыв
    val username: String, // Имя пользователя
    val rating: Int, // Рейтинг от 1 до 5
    val comment: String,
    val reviewDate: Date // Дата отзыва
)