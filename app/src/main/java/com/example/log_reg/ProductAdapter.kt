package com.example.log_reg

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.log_reg.data.CartItem
import com.example.log_reg.data.CartItemDao
import com.example.log_reg.data.Product
import com.example.log_reg.data.WishlistItem
import com.example.log_reg.data.WishlistItemDao

class ProductAdapter(
    private var productList: List<Product>,
    private val wishlistDao: WishlistItemDao,
    private val cartItemDao: CartItemDao,
    private val userId: Int
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val textViewProductName: TextView = itemView.findViewById(R.id.textViewProductName)
        val textViewProductPrice: TextView = itemView.findViewById(R.id.textViewProductPrice)
        val buttonBuy: Button = itemView.findViewById(R.id.buttonBuy)
        val imageViewWishlist: ImageView = itemView.findViewById(R.id.imageViewWishlist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // Відображення даних товару
        holder.textViewProductName.text = product.name
        holder.textViewProductPrice.text = "${product.price} грн"
        if (product.image.isNotEmpty()) {
            Glide.with(holder.itemView).load(product.image).into(holder.imageViewProduct)
        } else {
            holder.imageViewProduct.setImageResource(R.drawable.ic_launcher_background)
        }

        // Логіка кнопки "Купити"
        holder.buttonBuy.backgroundTintList = null
        holder.buttonBuy.setOnClickListener {
            Thread {
                val productId = product.id
                if (productId == null || productId <= 0) {
                    Log.e("ProductAdapter", "Невірний product.id: $productId")
                    return@Thread
                }
                try {
                    // Отримуємо всі записи кошика для користувача та шукаємо товар із поточним productId
                    val cartItems = cartItemDao.getCartItemsForUser(userId)
                    val existingItem = cartItems.find { it.productId == productId }
                    if (existingItem != null) {
                        // Якщо товар уже є, збільшуємо quantity на 1
                        val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
                        cartItemDao.insert(updatedItem)
                        Log.d("ProductAdapter", "Оновлено CartItem: $updatedItem")
                    } else {
                        // Якщо товару немає, вставляємо новий запис
                        val newItem = CartItem(
                            userId = userId,
                            productId = productId,
                            quantity = 1
                        )
                        cartItemDao.insert(newItem)
                        Log.d("ProductAdapter", "Вставлено новий CartItem: $newItem")
                    }
                    // Показуємо повідомлення користувачу на головному потоці
                    holder.itemView.post {
                        Toast.makeText(holder.itemView.context, "Товар додано до корзини", Toast.LENGTH_SHORT).show()
                    }
                } catch (ex: Exception) {
                    Log.e("ProductAdapter", "Помилка при роботі з корзиною: ${ex.message}")
                }
            }.start()
        }

        // Логіка роботи з вішлістом
        holder.imageViewWishlist.setOnClickListener {
            Thread {
                try {
                    val currentId = product.id ?: 0
                    val existingWishlistItem: WishlistItem? = wishlistDao.getWishlistItem(userId, currentId)
                    if (existingWishlistItem == null) {
                        val newWishlistItem = WishlistItem(userId = userId, productId = currentId)
                        wishlistDao.insert(newWishlistItem)
                        holder.itemView.post {
                            holder.imageViewWishlist.setImageResource(R.drawable.ic_heart_filled)
                        }
                        Log.d("ProductAdapter", "Успішно додано у вішліст: $newWishlistItem")
                    } else {
                        wishlistDao.delete(existingWishlistItem)
                        holder.itemView.post {
                            holder.imageViewWishlist.setImageResource(R.drawable.ic_heart_outline)
                        }
                        Log.d("ProductAdapter", "Видалено з вішліста: $existingWishlistItem")
                    }
                } catch (ex: Exception) {
                    Log.e("ProductAdapter", "Помилка роботи з вішлістом: ${ex.message}")
                }
            }.start()
        }

        // Оновлення іконки вішліста при прив'язці вью
        Thread {
            try {
                val currentId = product.id ?: 0
                val wishlistItem: WishlistItem? = wishlistDao.getWishlistItem(userId, currentId)
                holder.itemView.post {
                    if (wishlistItem != null) {
                        holder.imageViewWishlist.setImageResource(R.drawable.ic_heart_filled)
                    } else {
                        holder.imageViewWishlist.setImageResource(R.drawable.ic_heart_outline)
                    }
                }
            } catch (ex: Exception) {
                Log.e("ProductAdapter", "Помилка перевірки вішліста: ${ex.message}")
            }
        }.start()
    }

    fun updateList(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}
