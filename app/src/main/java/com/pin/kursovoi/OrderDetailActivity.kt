package com.pin.kursovoi
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var tvOrderId: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvAddress: TextView
    private lateinit var rvOrderItems: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        tvOrderId = findViewById(R.id.tv_order_detail_id)
        tvTotalAmount = findViewById(R.id.tv_order_detail_total)
        tvStatus = findViewById(R.id.tv_order_detail_status)
        tvDate = findViewById(R.id.tv_order_detail_date)
        tvAddress = findViewById(R.id.tv_order_detail_address)
        rvOrderItems = findViewById(R.id.rv_order_items)

        val orderId = intent.extras?.getLong("ORDER_ID", -1) ?: -1

        if (orderId == -1L) {
            finish()
            return
        }

        loadOrderDetails(orderId)
    }

    private fun loadOrderDetails(orderId: Long) {
        val dbHelper = (application as MyApplication).databaseHelper
        val order = dbHelper.getOrderById(orderId)
        val orderItems = dbHelper.getOrderItemsForOrder(orderId)

        if (order == null) {
            finish()
            return
        }

        tvOrderId.text = "Заказ #${order.orderId}"
        val formattedTotal = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(order.totalAmount)
        tvTotalAmount.text = "Итого: $formattedTotal"
        tvStatus.text = "Статус: ${order.status}"
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        tvDate.text = "Дата: ${dateFormat.format(order.orderDate)}"
        tvAddress.text = "Адрес доставки: ${order.shippingAddress}"

        rvOrderItems.layoutManager = LinearLayoutManager(this)
        val adapter = OrderItemAdapter(this, orderItems)
        rvOrderItems.adapter = adapter
    }
}

