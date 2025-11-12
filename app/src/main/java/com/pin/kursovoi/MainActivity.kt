package com.pin.kursovoi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pin.kursovoi.utils.PrefManager

class MainActivity : AppCompatActivity() {
    private lateinit var prefManager: PrefManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefManager = PrefManager(this)

        val welcomeText = findViewById<TextView>(R.id.tvWelcome)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        welcomeText.text = "Welcome, ${prefManager.getToken()?.let { "User" } ?: "Guest"}!"

        btnLogout.setOnClickListener {
            prefManager.clearToken()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }
}