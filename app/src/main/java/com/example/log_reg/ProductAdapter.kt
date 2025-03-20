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
import com.example.log_reg.data.WishlistItem
import com.example.log_reg.data.WishlistItemDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductAdapter(
    private var productList: List<Product>,
    private val wishlistDao: WishlistItemDao,
    private val userId: Int,
    private val scope: CoroutineScope  // Наприклад, scope з Activity чи Fragment
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val textViewProductName: TextView = itemView.findViewById(R.id.textViewProductName)
        val textViewProductPrice: TextView = itemView.findViewById(R.id.textViewProductPrice)
        val buttonBuy: Button = itemView.findViewById(R.id.buttonBuy)
        val imageViewWishlist: ImageView = itemView.findViewById(R.id.imageViewWishlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // Переконайтеся, що layout item_product.xml містить imageViewWishlist
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // Встановлюємо дані товару
        holder.textViewProductName.text = product.name
        holder.textViewProductPrice.text = "${product.price} грн"

        // Завантаження зображення через Glide
        if (product.image.isNotEmpty()) {
            Glide.with(holder.itemView)
                .load(product.image)
                .into(holder.imageViewProduct)
        } else {
            holder.imageViewProduct.setImageResource(R.drawable.ic_launcher_background)
        }

        // Налаштовуємо вигляд кнопки "Купити"
        holder.buttonBuy.backgroundTintList = null
        holder.buttonBuy.setBackgroundResource(R.drawable.button_red)
        holder.buttonBuy.setTextColor(holder.itemView.resources.getColor(android.R.color.white, null))
        holder.buttonBuy.setOnClickListener {
            // Логіка при натисканні "Купити"
        }

        // Асинхронна перевірка чи товар знаходиться у вішлісті та встановлення відповідної іконки
        scope.launch {
            val wishlistItem = withContext(Dispatchers.IO) {
                wishlistDao.getWishlistItem(userId, product.id ?: 0)
            }
            if (wishlistItem != null) {
                holder.imageViewWishlist.setImageResource(R.drawable.ic_heart_filled)
            } else {
                holder.imageViewWishlist.setImageResource(R.drawable.ic_heart_outline)
            }
        }

        // Обробка кліку на іконку сердечка
        holder.imageViewWishlist.setOnClickListener {
            scope.launch {
                val existingItem = withContext(Dispatchers.IO) {
                    wishlistDao.getWishlistItem(userId, product.id ?: 0)
                }
                if (existingItem == null) {
                    // Додаємо товар до вішлісту
                    val newItem = WishlistItem(userId = userId, productId = product.id ?: 0)
                    withContext(Dispatchers.IO) {
                        wishlistDao.insert(newItem)
                    }
                    holder.imageViewWishlist.setImageResource(R.drawable.ic_heart_filled)
                } else {
                    // Видаляємо товар з вішлісту
                    withContext(Dispatchers.IO) {
                        wishlistDao.delete(existingItem)
                    }
                    holder.imageViewWishlist.setImageResource(R.drawable.ic_heart_outline)
                }
            }
        }
    }

    // Оновлення списку продуктів
    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}
