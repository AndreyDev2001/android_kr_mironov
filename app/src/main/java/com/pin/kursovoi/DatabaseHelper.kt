package com.pin.kursovoi

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Date

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val context: Context = context
    private val dbPath: String = context.getDatabasePath(DATABASE_NAME).absolutePath

    companion object {
        private const val DATABASE_NAME = "kursovoi.db"
        private const val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Не используется, база данных уже создана в assets
        // Этот метод вызывается SQLiteOpenHelper, если базы нет, но мы её копируем вручную
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Обработка обновления базы данных
        // Удаляем старую базу данных и копируем новую из assets
        try {
            context.deleteDatabase(DATABASE_NAME) // Удаляет файл базы данных
            // После удаления, при следующем вызове getWritableDatabase/getReadableDatabase
            // сработает проверка checkDatabase и copyDatabase
        } catch (e: IOException) {
            throw Error("Ошибка обновления базы данных: ${e.message}")
        }
    }

    // Вспомогательная функция для проверки существования базы данных
    private fun checkDatabase(): Boolean {
        val dbFile = java.io.File(dbPath)
        return dbFile.exists()
    }

    // Вспомогательная функция для копирования базы данных из assets
    @Throws(IOException::class)
    private fun copyDatabase() {
        val inputStream: InputStream = context.assets.open(DATABASE_NAME)
        val outputStream = FileOutputStream(dbPath)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    // Переопределяем getWritableDatabase
    override fun getWritableDatabase(): SQLiteDatabase {
        // Проверяем, существует ли база данных
        if (!checkDatabase()) {
            // Если нет, копируем из assets
            try {
                copyDatabase()
            } catch (e: IOException) {
                throw Error("Не удалось скопировать базу данных: ${e.message}")
            }
        }
        // Теперь открываем базу данных в режиме для записи
        return super.getWritableDatabase()
    }

    // Переопределяем getReadableDatabase
    override fun getReadableDatabase(): SQLiteDatabase {
        // Аналогично, проверяем и копируем, если нужно
        if (!checkDatabase()) {
            try {
                copyDatabase()
            } catch (e: IOException) {
                throw Error("Не удалось скопировать базу данных: ${e.message}")
            }
        }
        // Открываем в режиме для чтения (который также позволяет запись, если нужно)
        return super.getReadableDatabase()
    }

    // --- Методы для работы с пользователями ---
    fun getUserByUsername(username: String): User? {
        val db = readableDatabase // Вызовет getReadableDatabase, который проверит и скопирует, если нужно
        val cursor = db.query(
            "users",
            arrayOf("user_id", "username", "email", "password_hash"),
            "username = ?",
            arrayOf(username),
            null, null, null
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                userId = cursor.getLong(cursor.getColumnIndexOrThrow("user_id")),
                username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                passwordHash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"))
            )
        }
        cursor.close()
        return user
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            "users",
            arrayOf("user_id", "username", "email", "password_hash"),
            "email = ?",
            arrayOf(email),
            null, null, null
        )
        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                userId = cursor.getLong(cursor.getColumnIndexOrThrow("user_id")),
                username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                passwordHash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"))
            )
        }
        cursor.close()
        return user
    }

    fun insertUser(user: User): Long {
        val db = writableDatabase // Вызовет getWritableDatabase, который проверит и скопирует, если нужно
        val values = android.content.ContentValues().apply {
            put("username", user.username)
            put("email", user.email)
            put("password_hash", user.passwordHash)
            put("created_at", user.createdAt)
        }
        return db.insert("users", null, values)
    }

    // --- Методы для работы с продуктами ---
    fun getProductsByCategory(categoryId: Long): List<Product> {
        val db = readableDatabase
        val cursor = db.query(
            "products",
            arrayOf("product_id", "name", "description", "price", "stock_quantity", "category_id", "image_url"),
            "category_id = ?",
            arrayOf(categoryId.toString()),
            null, null, "name ASC"
        )

        val products = mutableListOf<Product>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("product_id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
                val stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity"))
                val catId = cursor.getLong(cursor.getColumnIndexOrThrow("category_id"))
                val img = cursor.getString(cursor.getColumnIndexOrThrow("image_url"))

                products.add(Product(id, name, desc, price, stock, catId, img))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return products
    }

    fun getAllProducts(): List<Product> {
        val db = readableDatabase
        val cursor = db.query(
            "products",
            arrayOf("product_id", "name", "description", "price", "stock_quantity", "category_id", "image_url"),
            null, null, null, null, "name ASC"
        )

        val products = mutableListOf<Product>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("product_id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
                val stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity"))
                val catId = cursor.getLong(cursor.getColumnIndexOrThrow("category_id"))
                val img = cursor.getString(cursor.getColumnIndexOrThrow("image_url"))

                products.add(Product(id, name, desc, price, stock, catId, img))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return products
    }

    fun getProductById(productId: Long): Product? {
        val db = readableDatabase
        val cursor = db.query(
            "products",
            arrayOf("product_id", "name", "description", "price", "stock_quantity", "category_id", "image_url"),
            "product_id = ?",
            arrayOf(productId.toString()),
            null, null, null
        )

        var product: Product? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("product_id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val desc = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
            val stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock_quantity"))
            val catId = cursor.getLong(cursor.getColumnIndexOrThrow("category_id"))
            val img = cursor.getString(cursor.getColumnIndexOrThrow("image_url"))

            product = Product(id, name, desc, price, stock, catId, img)
        }
        cursor.close()
        return product
    }

    // --- Методы для работы с категориями ---
    fun getAllCategories(): List<Category> {
        val db = readableDatabase
        val cursor = db.query(
            "categories",
            arrayOf("category_id", "name", "description"),
            null, null, null, null, "name ASC"
        )

        val categories = mutableListOf<Category>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("category_id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val desc = cursor.getString(cursor.getColumnIndexOrThrow("description"))

                categories.add(Category(id, name, desc))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categories
    }

    // --- Методы для работы с корзиной ---
    fun addToCart(userId: Long, productId: Long, quantity: Int): Long {
        val db = writableDatabase
        val existingCursor = db.query(
            "carts",
            arrayOf("quantity"),
            "user_id = ? AND product_id = ?",
            arrayOf(userId.toString(), productId.toString()),
            null, null, null
        )

        var newRowId: Long = -1
        if (existingCursor.moveToFirst()) {
            val currentQty = existingCursor.getInt(existingCursor.getColumnIndexOrThrow("quantity"))
            val newQty = currentQty + quantity
            existingCursor.close()

            newRowId = db.update(
                "carts",
                android.content.ContentValues().apply { put("quantity", newQty) },
                "user_id = ? AND product_id = ?",
                arrayOf(userId.toString(), productId.toString())
            ).toLong()
        } else {
            existingCursor.close()
            val values = android.content.ContentValues().apply {
                put("user_id", userId)
                put("product_id", productId)
                put("quantity", quantity)
            }
            newRowId = db.insert("carts", null, values)
        }
        return newRowId
    }

    fun getCartItemsForUser(userId: Long): List<CartItem> {
        val db = readableDatabase
        val cursor = db.query(
            "carts c JOIN products p ON c.product_id = p.product_id",
            arrayOf("c.cart_id", "c.user_id", "c.product_id", "c.quantity", "p.name", "p.price", "p.image_url"),
            "c.user_id = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        val cartItems = mutableListOf<CartItem>()
        if (cursor.moveToFirst()) {
            do {
                val cartId = cursor.getLong(cursor.getColumnIndexOrThrow("cart_id"))
                val userIdFromDb = cursor.getLong(cursor.getColumnIndexOrThrow("user_id"))
                val productId = cursor.getLong(cursor.getColumnIndexOrThrow("product_id"))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                val productName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val productPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
                val productImageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"))

                cartItems.add(CartItem(cartId, userIdFromDb, productId, quantity, productName, productPrice, productImageUrl))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return cartItems
    }

    fun removeFromCart(cartId: Long): Int {
        val db = writableDatabase
        return db.delete("carts", "cart_id = ?", arrayOf(cartId.toString()))
    }

    fun clearCartForUser(userId: Long) {
        val db = writableDatabase
        db.delete("carts", "user_id = ?", arrayOf(userId.toString()))
    }

    // --- Методы для работы с заказами ---
    fun insertOrder(userId: Long, totalAmount: Double, shippingAddress: String): Long {
        val db = writableDatabase
        val values = android.content.ContentValues().apply {
            put("user_id", userId)
            put("total_amount", totalAmount)
            put("shipping_address", shippingAddress)
            put("status", "pending")
            put("order_date", System.currentTimeMillis().toString())
        }
        return db.insert("orders", null, values)
    }

    fun insertOrderItem(orderId: Long, productId: Long, quantity: Int, unitPrice: Double) {
        val db = writableDatabase
        val values = android.content.ContentValues().apply {
            put("order_id", orderId)
            put("product_id", productId)
            put("quantity", quantity)
            put("unit_price", unitPrice)
        }
        db.insert("order_items", null, values)
    }

    fun getOrdersForUser(userId: Long): List<Order> {
        val db = readableDatabase
        val cursor = db.query(
            "orders",
            arrayOf("order_id", "user_id", "total_amount", "status", "order_date", "shipping_address"),
            "user_id = ?",
            arrayOf(userId.toString()),
            null, null, "order_date DESC"
        )

        val orders = mutableListOf<Order>()
        if (cursor.moveToFirst()) {
            do {
                val orderId = cursor.getLong(cursor.getColumnIndexOrThrow("order_id"))
                val userIdFromDb = cursor.getLong(cursor.getColumnIndexOrThrow("user_id"))
                val totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"))
                val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
                val dateStr = cursor.getString(cursor.getColumnIndexOrThrow("order_date"))
                val address = cursor.getString(cursor.getColumnIndexOrThrow("shipping_address"))
                val date = java.util.Date(dateStr.toLongOrNull() ?: System.currentTimeMillis())

                orders.add(Order(orderId, userIdFromDb, totalAmount, status, date, address))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return orders
    }

    fun getOrderById(orderId: Long): Order? {
        val db = readableDatabase
        val cursor = db.query(
            "orders",
            arrayOf("order_id", "user_id", "total_amount", "status", "order_date", "shipping_address"),
            "order_id = ?",
            arrayOf(orderId.toString()),
            null, null, null
        )

        var order: Order? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("order_id"))
            val userId = cursor.getLong(cursor.getColumnIndexOrThrow("user_id"))
            val totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            val dateStr = cursor.getString(cursor.getColumnIndexOrThrow("order_date"))
            val address = cursor.getString(cursor.getColumnIndexOrThrow("shipping_address"))
            val date = java.util.Date(dateStr.toLongOrNull() ?: System.currentTimeMillis())

            order = Order(id, userId, totalAmount, status, date, address)
        }
        cursor.close()
        return order
    }

    fun getOrderItemsForOrder(orderId: Long): List<OrderItem> {
        val db = readableDatabase
        val cursor = db.query(
            "order_items oi JOIN products p ON oi.product_id = p.product_id",
            arrayOf("oi.order_item_id", "oi.order_id", "oi.product_id", "oi.quantity", "oi.unit_price", "p.name", "p.image_url"),
            "oi.order_id = ?",
            arrayOf(orderId.toString()),
            null, null, null
        )

        val orderItems = mutableListOf<OrderItem>()
        if (cursor.moveToFirst()) {
            do {
                val orderItemId = cursor.getLong(cursor.getColumnIndexOrThrow("order_item_id"))
                val orderIdFromDb = cursor.getLong(cursor.getColumnIndexOrThrow("order_id"))
                val productId = cursor.getLong(cursor.getColumnIndexOrThrow("product_id"))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                val unitPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("unit_price"))
                val productName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val productImageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"))

                orderItems.add(OrderItem(orderItemId, orderIdFromDb, productId, quantity, unitPrice, productName, productImageUrl))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return orderItems
    }

    // --- Методы для работы с отзывами ---
    fun insertReview(review: Review): Long {
        val db = writableDatabase
        val values = android.content.ContentValues().apply {
            put("product_id", review.productId)
            put("user_id", review.userId)
            put("rating", review.rating)
            put("comment", review.comment)
            put("review_date", review.reviewDate.time.toString())
        }
        return db.insert("reviews", null, values)
    }

    fun getReviewsByProductId(productId: Long): List<Review> {
        val db = readableDatabase
        // Присоединяем таблицу users к reviews по user_id
        val cursor = db.query(
            "reviews r JOIN users u ON r.user_id = u.user_id", // <-- JOIN
            arrayOf("r.review_id", "r.product_id", "r.user_id", "u.username", "r.rating", "r.comment", "r.review_date"), // <-- u.username
            "r.product_id = ?", // <-- Уточняем, что product_id из таблицы reviews
            arrayOf(productId.toString()),
            null, null, "r.review_date DESC" // <-- Уточняем, что сортировка по дате из reviews
        )

        val reviews = mutableListOf<Review>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("review_id"))
                val prodId = cursor.getLong(cursor.getColumnIndexOrThrow("product_id"))
                val userId = cursor.getLong(cursor.getColumnIndexOrThrow("user_id"))
                val username = cursor.getString(cursor.getColumnIndexOrThrow("username")) // <-- Теперь username доступен
                val rating = cursor.getInt(cursor.getColumnIndexOrThrow("rating"))
                val comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"))
                val dateStr = cursor.getString(cursor.getColumnIndexOrThrow("review_date"))
                val date = java.util.Date(dateStr.toLongOrNull() ?: System.currentTimeMillis())

                reviews.add(Review(id, prodId, userId, username, rating, comment, date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return reviews
    }
}

// User data class
data class User(
    val userId: Long,
    val username: String,
    val email: String,
    val passwordHash: String,
    val createdAt: String = java.util.Date().toString()
)

// CartItem data class
data class CartItem(
    val cartId: Long,
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String?
)

// Order data class
data class Order(
    val orderId: Long,
    val userId: Long,
    val totalAmount: Double,
    val status: String,
    val orderDate: java.util.Date,
    val shippingAddress: String
)

// OrderItem data class
data class OrderItem(
    val orderItemId: Long,
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double,
    val productName: String,
    val productImageUrl: String?
)
