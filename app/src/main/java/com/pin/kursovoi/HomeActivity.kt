package com.pin.kursovoi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

class HomeActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory((application as MyApplication).authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val welcomeMessageTextView = findViewById<TextView>(R.id.tv_welcome_message)
        val goToProductsButton = findViewById<Button>(R.id.btn_go_to_products)
        val logoutButton = findViewById<Button>(R.id.btn_logout)
        val btnGoToCart = findViewById<Button>(R.id.btn_go_to_cart)
        val btnGoToOrderHistory = findViewById<Button>(R.id.btn_go_to_order_history)

        // Подписываемся на LiveData для обновления приветствия
        authViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                val username = (application as MyApplication).authRepository.getCurrentUsername()
                welcomeMessageTextView.text = "Привет, $username!"
            } else {
                navigateToAuthScreen()
            }
        }

        // Обработчик кнопки "Перейти к товарам"
        goToProductsButton.setOnClickListener {
            println("Кнопка 'Перейти к товарам' нажата!")
            Toast.makeText(this, "Открываю список товаров...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ProductListActivity::class.java)
            startActivity(intent)
        }

        // Обработчики новых кнопок
        btnGoToCart.setOnClickListener {
            val intent = Intent(this, OrderActivity::class.java)
            startActivity(intent)
        }

        btnGoToOrderHistory.setOnClickListener {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            startActivity(intent)
        }


        // Обработчик кнопки "Выйти"
        logoutButton.setOnClickListener {
            authViewModel.logout()
        }
    }

    private fun navigateToAuthScreen() {
        val intent = Intent(this, MainActivity::class.java) // или LoginActivity, если вы используете отдельную активность
        startActivity(intent)
        finish() // Закрываем HomeActivity, чтобы пользователь не мог вернуться назад на неё без входа
    }
}