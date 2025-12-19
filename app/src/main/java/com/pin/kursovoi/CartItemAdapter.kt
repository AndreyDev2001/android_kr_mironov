package com.pin.kursovoi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.*

class CartItemAdapter(
    private val context: Context,
    private val cartItemList: List<CartItem>,
    private val onRemoveClick: (CartItem) -> Unit // Lambda для обработки удаления
) : RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_cart_item_image)
        val nameTextView: TextView = itemView.findViewById(R.id.tv_cart_item_name)
        val priceTextView: TextView = itemView.findViewById(R.id.tv_cart_item_price)
        val quantityTextView: TextView = itemView.findViewById(R.id.tv_cart_item_quantity)
        val removeButton: Button = itemView.findViewById(R.id.btn_remove_from_cart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false)
        return CartItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val item = cartItemList[position]

        holder.nameTextView.text = item.productName
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(item.productPrice * item.quantity)
        holder.priceTextView.text = formattedPrice
        holder.quantityTextView.text = "Кол-во: ${item.quantity}"

        Glide.with(context)
            .load(item.productImageUrl)
            .placeholder(R.drawable.loading)
            .error(R.drawable.error)
            .into(holder.imageView)

        holder.removeButton.setOnClickListener {
            onRemoveClick(item)
        }
    }

    override fun getItemCount(): Int = cartItemList.size
}