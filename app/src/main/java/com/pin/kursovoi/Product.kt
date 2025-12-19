package com.pin.kursovoi

data class Product(
    val productId: Long,
    val name: String,
    val description: String,
    val price: Double,
    val stockQuantity: Int,
    val categoryId: Long,
    val imageUrl: String?
)
