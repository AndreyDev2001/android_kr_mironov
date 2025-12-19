package com.pin.kursovoi

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(private val context: Context, private val productList: List<Product>, private val onItemClick: (Product) -> Unit ) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.iv_product_image)
        val nameTextView: TextView = itemView.findViewById(R.id.tv_product_name)
        val priceTextView: TextView = itemView.findViewById(R.id.tv_product_price)
        val detailsButton: Button = itemView.findViewById(R.id.btn_view_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.nameTextView.text = product.name
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(product.price)
        holder.priceTextView.text = formattedPrice

        // Загрузка изображения
        Glide.with(context)
            .load(product.imageUrl)
            .placeholder(R.drawable.loading) // Заглушка
            .error(R.drawable.error) // Заглушка на случай ошибки
            .into(holder.imageView)

        // Обработка нажатия на кнопку "Подробнее"
        holder.detailsButton.setOnClickListener {
            onItemClick(product) // Вызываем переданную lambda
        }
    }

    override fun getItemCount(): Int = productList.size
}