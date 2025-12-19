package com.pin.kursovoi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var rvOrders: RecyclerView

    private var userId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        rvOrders = findViewById(R.id.rv_orders)

        // Получаем ID текущего пользователя
        val authRepository = (application as MyApplication).authRepository
        if (!authRepository.isLoggedIn()) {
            Toast.makeText(this, "Сессия истекла.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        userId = getSharedPreferences("auth_prefs", MODE_PRIVATE).getLong("user_id", -1)

        loadOrderHistory()
    }

    private fun loadOrderHistory() {
        val dbHelper = (application as MyApplication).databaseHelper
        val orders = dbHelper.getOrdersForUser(userId)

        rvOrders.layoutManager = LinearLayoutManager(this)
        val adapter = OrderHistoryAdapter(this, orders) { order ->
            // Перейти к деталям заказа
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("ORDER_ID", order.orderId)
            startActivity(intent)
        }
        rvOrders.adapter = adapter
    }
}