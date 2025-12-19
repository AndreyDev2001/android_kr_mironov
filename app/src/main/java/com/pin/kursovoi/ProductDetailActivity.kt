package com.pin.kursovoi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

class ProductDetailActivity : AppCompatActivity(){
    private lateinit var ivDetailImage: ImageView
    private lateinit var tvDetailName: TextView
    private lateinit var tvDetailPrice: TextView
    private lateinit var tvDetailDescription: TextView
    private lateinit var tvDetailStock: TextView
    private lateinit var btnAddToCart: Button
    private lateinit var rbReviewRating: RatingBar
    private lateinit var etReviewComment: EditText
    private lateinit var btnSubmitReview: Button
    private lateinit var rvReviews: RecyclerView

    private var productId: Long = -1
    private var userId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        // Инициализация Views
        ivDetailImage = findViewById(R.id.iv_detail_image)
        tvDetailName = findViewById(R.id.tv_detail_name)
        tvDetailPrice = findViewById(R.id.tv_detail_price)
        tvDetailDescription = findViewById(R.id.tv_detail_description)
        tvDetailStock = findViewById(R.id.tv_detail_stock)
        btnAddToCart = findViewById(R.id.btn_add_to_cart)
        rbReviewRating = findViewById(R.id.rb_review_rating)
        etReviewComment = findViewById(R.id.et_review_comment)
        btnSubmitReview = findViewById(R.id.btn_submit_review)
        rvReviews = findViewById(R.id.rv_reviews)

        productId = intent.extras?.getLong("PRODUCT_ID", -1) ?: -1

        if (productId == -1L) {
            finish()
            return
        }

        // Получаем ID текущего пользователя из SharedPreferences через AuthRepository
        val authRepository = (application as MyApplication).authRepository
        if (!authRepository.isLoggedIn()) {
            btnAddToCart.isEnabled = false
            btnSubmitReview.isEnabled = false
            Toast.makeText(this, "Войдите, чтобы добавить в корзину или оставить отзыв.", Toast.LENGTH_LONG).show()
        } else {
            userId = getSharedPreferences("auth_prefs", MODE_PRIVATE).getLong("user_id", -1)
        }

        loadProductDetails()
        loadReviews()

        btnAddToCart.setOnClickListener {
            if (userId != -1L) {
                addToCartAndNavigate()
            } else {
                Toast.makeText(this, "Сначала войдите в аккаунт.", Toast.LENGTH_SHORT).show()
            }
        }

        btnSubmitReview.setOnClickListener {
            if (userId != -1L) {
                submitReview(authRepository.getCurrentUsername() ?: "Аноним") // Получаем имя
            } else {
                Toast.makeText(this, "Сначала войдите в аккаунт.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProductDetails() {
        val dbHelper = (application as MyApplication).databaseHelper
        val product = dbHelper.getProductById(productId)

        if (product == null) {
            finish()
            return
        }

        tvDetailName.text = product.name
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(product.price)
        tvDetailPrice.text = formattedPrice
        tvDetailDescription.text = product.description
        tvDetailStock.text = "В наличии: ${product.stockQuantity} шт."

        Glide.with(this)
            .load(product.imageUrl)
            .placeholder(R.drawable.loading)
            .error(R.drawable.error)
            .into(ivDetailImage)
    }

    private fun loadReviews() {
        val dbHelper = (application as MyApplication).databaseHelper
        val reviews = dbHelper.getReviewsByProductId(productId)

        rvReviews.layoutManager = LinearLayoutManager(this)
        val adapter = ReviewAdapter(this, reviews)
        rvReviews.adapter = adapter
    }

    private fun addToCartAndNavigate() {
        val dbHelper = (application as MyApplication).databaseHelper
        // Добавляем товар (ID текущего пользователя и ID товара из этой активности)
        val result = dbHelper.addToCart(userId, productId, 1) // Добавляем 1 шт.

        if (result != -1L) {
            Toast.makeText(this, "Товар добавлен в корзину!", Toast.LENGTH_SHORT).show()
            // Перенаправляем на экран корзины
            val intent = Intent(this, OrderActivity::class.java)
            startActivity(intent)
            // finish() - опционально, если не хотите возвращаться обратно на детали сразу
        } else {
            Toast.makeText(this, "Ошибка при добавлении в корзину.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitReview(username: String) {
        val rating = rbReviewRating.rating.toInt()
        val comment = etReviewComment.text.toString().trim()

        if (rating < 1) {
            Toast.makeText(this, "Пожалуйста, поставьте оценку.", Toast.LENGTH_SHORT).show()
            return
        }

        if (comment.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите комментарий.", Toast.LENGTH_SHORT).show()
            return
        }

        val review = Review(
            reviewId = 0, // будет установлен базой данных
            productId = productId,
            userId = userId,
            username = username, // Используем имя из AuthRepository
            rating = rating,
            comment = comment,
            reviewDate = Date() // Текущая дата
        )

        val dbHelper = (application as MyApplication).databaseHelper
        val result = dbHelper.insertReview(review)

        if (result != -1L) {
            Toast.makeText(this, "Отзыв отправлен!", Toast.LENGTH_SHORT).show()
            // Очистить поля ввода
            rbReviewRating.rating = 0f
            etReviewComment.setText("")
            // Обновить список отзывов
            loadReviews()
        } else {
            Toast.makeText(this, "Ошибка при отправке отзыва.", Toast.LENGTH_SHORT).show()
        }
    }
}