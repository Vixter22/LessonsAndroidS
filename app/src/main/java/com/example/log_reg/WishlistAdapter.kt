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
import com.example.log_reg.data.WishlistItemDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WishlistAdapter(
    private var productList: List<Product>,
    private val wishlistDao: WishlistItemDao,
    private val userId: Int,
    private val scope: CoroutineScope
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val textViewProductName: TextView = itemView.findViewById(R.id.textViewProductName)
        val textViewProductPrice: TextView = itemView.findViewById(R.id.textViewProductPrice)
        // Іконка "сердечко" використовується для видалення товару з вішліста
        val imageViewRemove: ImageView = itemView.findViewById(R.id.imageViewWishlist)
        val buttonBuy: Button = itemView.findViewById(R.id.buttonBuy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return WishlistViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val product = productList[position]
        holder.textViewProductName.text = product.name
        holder.textViewProductPrice.text = "${product.price} грн"

        if (product.image.isNotEmpty()) {
            Glide.with(holder.itemView)
                .load(product.image)
                .into(holder.imageViewProduct)
        } else {
            holder.imageViewProduct.setImageResource(R.drawable.ic_launcher_background)
        }

        // Оскільки товар знаходиться у вішлісті, відображаємо заповнену іконку
        holder.imageViewRemove.setImageResource(R.drawable.ic_heart_filled)

        // Налаштовуємо кнопку "Купити"
        holder.buttonBuy.backgroundTintList = null
        holder.buttonBuy.setBackgroundResource(R.drawable.button_red)
        holder.buttonBuy.setTextColor(holder.itemView.resources.getColor(android.R.color.white, null))
        holder.buttonBuy.setOnClickListener {
            // Логіка для покупки товару (наприклад, перехід на екран оформлення покупки)
        }

        // При кліку на сердечко видаляємо товар з вішліста для даного userId
        holder.imageViewRemove.setOnClickListener {
            scope.launch {
                val wishlistItem = withContext(Dispatchers.IO) {
                    wishlistDao.getWishlistItem(userId, product.id ?: 0)
                }
                if (wishlistItem != null) {
                    withContext(Dispatchers.IO) {
                        wishlistDao.delete(wishlistItem)
                    }
                }
            }
        }
    }

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}
