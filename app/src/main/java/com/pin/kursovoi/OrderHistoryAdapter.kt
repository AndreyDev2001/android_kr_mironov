package com.pin.kursovoi

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(
    private val context: Context,
    private val orderList: List<Order>,
    private val onItemClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderIdTextView: TextView = itemView.findViewById(R.id.tv_order_id)
        val totalAmountTextView: TextView = itemView.findViewById(R.id.tv_order_total)
        val statusTextView: TextView = itemView.findViewById(R.id.tv_order_status)
        val dateTextView: TextView = itemView.findViewById(R.id.tv_order_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]

        holder.orderIdTextView.text = "Заказ #${order.orderId}"
        val formattedTotal = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(order.totalAmount)
        holder.totalAmountTextView.text = "Сумма: $formattedTotal"
        holder.statusTextView.text = "Статус: ${order.status}"
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.dateTextView.text = "Дата: ${dateFormat.format(order.orderDate)}"

        holder.itemView.setOnClickListener {
            onItemClick(order)
        }
    }

    override fun getItemCount(): Int = orderList.size
}