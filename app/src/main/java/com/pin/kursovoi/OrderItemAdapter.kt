package com.pin.kursovoi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.*

class OrderItemAdapter(
    private val context: Context,
    private val orderItemList: List<OrderItem>
) : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_order_item_image)
        val nameTextView: TextView = itemView.findViewById(R.id.tv_order_item_name)
        val priceTextView: TextView = itemView.findViewById(R.id.tv_order_item_price)
        val quantityTextView: TextView = itemView.findViewById(R.id.tv_order_item_quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_order_item, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val item = orderItemList[position]

        holder.nameTextView.text = item.productName
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(item.unitPrice * item.quantity)
        holder.priceTextView.text = "Цена: $formattedPrice"
        holder.quantityTextView.text = "Кол-во: ${item.quantity}"

        Glide.with(context)
            .load(item.productImageUrl)
            .placeholder(R.drawable.loading)
            .error(R.drawable.error)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = orderItemList.size
}