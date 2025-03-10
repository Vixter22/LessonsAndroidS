package com.example.log_reg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.log_reg.data.Product

class ProductAdapter(
    private var productList: List<Product>
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val textViewProductName: TextView = itemView.findViewById(R.id.textViewProductName)
        val textViewProductPrice: TextView = itemView.findViewById(R.id.textViewProductPrice)
        val buttonBuy: Button = itemView.findViewById(R.id.buttonBuy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.buttonBuy.backgroundTintList = null
        holder.textViewProductName.text = product.name
        holder.textViewProductPrice.text = "${product.price} грн"

        // Завантаження зображення з використанням Glide, якщо URL не порожній
        if (product.image.isNotEmpty()) {
            Glide.with(holder.itemView)
                .load(product.image)
                .into(holder.imageViewProduct)
        } else {
            // Встановлюємо дефолтне зображення, якщо URL порожній
            holder.imageViewProduct.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.buttonBuy.setBackgroundResource(R.drawable.button_red)
        holder.buttonBuy.setTextColor(holder.itemView.resources.getColor(android.R.color.white, null))

        holder.buttonBuy.setOnClickListener {
            // Логіка при натисканні "Купити"
        }
    }

    // Оновлюємо список продуктів
    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}
