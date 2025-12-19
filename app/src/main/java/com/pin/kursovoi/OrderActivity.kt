package com.pin.kursovoi

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.*

class OrderActivity : AppCompatActivity() {

    private lateinit var rvCartItems: RecyclerView
    private lateinit var tvTotalAmount: TextView
    private lateinit var etShippingAddress: TextInputEditText
    private lateinit var btnPlaceOrder: Button

    private var userId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        rvCartItems = findViewById(R.id.rv_cart_items)
        tvTotalAmount = findViewById(R.id.tv_total_amount)
        etShippingAddress = findViewById(R.id.et_shipping_address)
        btnPlaceOrder = findViewById(R.id.btn_place_order)

        // Получаем ID текущего пользователя
        val authRepository = (application as MyApplication).authRepository
        if (!authRepository.isLoggedIn()) {
            Toast.makeText(this, "Сессия истекла.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        userId = getSharedPreferences("auth_prefs", MODE_PRIVATE).getLong("user_id", -1)

        loadCartItems()

        btnPlaceOrder.setOnClickListener {
            placeOrder()
        }
    }

    private fun loadCartItems() {
        val dbHelper = (application as MyApplication).databaseHelper
        val cartItems = dbHelper.getCartItemsForUser(userId)

        rvCartItems.layoutManager = LinearLayoutManager(this)
        val adapter = CartItemAdapter(this, cartItems) { cartItem ->
            dbHelper.removeFromCart(cartItem.cartId)
            loadCartItems()
        }
        rvCartItems.adapter = adapter

        // Вычисляем итоговую сумму
        val total = cartItems.sumOf { item -> item.productPrice * item.quantity }
        val formattedTotal = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(total)
        tvTotalAmount.text = "Итого: $formattedTotal"
    }

    private fun placeOrder() {
        val address = etShippingAddress.text.toString().trim()
        if (address.isEmpty()) {
            Toast.makeText(this, "Введите адрес доставки.", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = (application as MyApplication).databaseHelper
        val cartItems = dbHelper.getCartItemsForUser(userId)

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Корзина пуста.", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = cartItems.sumOf { it.productPrice * it.quantity }

        // 1. Создать заказ
        val orderId = dbHelper.insertOrder(userId, totalAmount, address)

        if (orderId == -1L) {
            Toast.makeText(this, "Ошибка при создании заказа.", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Добавить элементы заказа
        for (item in cartItems) {
            dbHelper.insertOrderItem(orderId, item.productId, item.quantity, item.productPrice)
        }

        // 3. Очистить корзину
        dbHelper.clearCartForUser(userId)

        Toast.makeText(this, "Заказ оформлен успешно!", Toast.LENGTH_LONG).show()
        finish() // Возвращаемся на предыдущую активность (например, HomeActivity)
    }
}