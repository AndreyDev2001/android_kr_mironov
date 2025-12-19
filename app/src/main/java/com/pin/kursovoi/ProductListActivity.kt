package com.pin.kursovoi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProductListActivity : AppCompatActivity(){
    private lateinit var rvProducts: RecyclerView
    private lateinit var rvCategories: RecyclerView

    private lateinit var dbHelper: DatabaseHelper
    private var selectedCategoryId: Long = -1

    // Добавим тег для логов
    companion object {
        private const val TAG = "ProductListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        Log.d(TAG, "onCreate вызвана")

        rvProducts = findViewById(R.id.rv_products)
        rvCategories = findViewById(R.id.rv_categories)

        try {
            // Убедитесь, что MyApplication доступна и содержит dbHelper
            dbHelper = (application as MyApplication).databaseHelper
            Log.d(TAG, "DatabaseHelper получен успешно")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении DatabaseHelper: ${e.message}", e)
            Toast.makeText(this, "Ошибка инициализации: ${e.message}", Toast.LENGTH_LONG).show()
            finish() // Закрываем активность, если база данных недоступна
            return
        }

        setupCategoryList()
        loadProducts(selectedCategoryId)
    }

    private fun setupCategoryList() {
        Log.d(TAG, "setupCategoryList вызвана")
        try {
            val categories = dbHelper.getAllCategories().toMutableList()
            Log.d(TAG, "Получено ${categories.size} категорий из базы данных")
            categories.add(0, Category(-1L, "Все категории", null))

            rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            val categoryAdapter = CategoryAdapter(categories) { category ->
                Log.d(TAG, "Выбрана категория: ${category.name}")
                selectedCategoryId = category.categoryId
                loadProducts(selectedCategoryId)
            }
            rvCategories.adapter = categoryAdapter
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при настройке списка категорий: ${e.message}", e)
            Toast.makeText(this, "Ошибка загрузки категорий: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadProducts(categoryId: Long) {
        Log.d(TAG, "loadProducts вызвана для categoryId: $categoryId")
        try {
            val products = if (categoryId == -1L) {
                dbHelper.getAllProducts()
            } else {
                dbHelper.getProductsByCategory(categoryId)
            }
            Log.d(TAG, "Получено ${products.size} товаров")

            rvProducts.layoutManager = LinearLayoutManager(this)
            val productAdapter = ProductAdapter(this, products) { product ->
                val intent = Intent(this, ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_ID", product.productId)
                startActivity(intent)
            }
            rvProducts.adapter = productAdapter
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при загрузке товаров: ${e.message}", e)
            Toast.makeText(this, "Ошибка загрузки товаров: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}