package com.example.log_reg

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
import com.example.log_reg.data.WishlistItemDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WishlistAdapter(
    private var productList: List<Product>,
    private val wishlistDao: WishlistItemDao,
    private val cartItemDao: CartItemDao,
    private val userId: Int,
    private val scope: CoroutineScope
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val textViewProductName: TextView = itemView.findViewById(R.id.textViewProductName)
        val textViewProductPrice: TextView = itemView.findViewById(R.id.textViewProductPrice)
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

        // Вішлістний товар показуємо з заповненим сердечком
        holder.imageViewRemove.setImageResource(R.drawable.ic_heart_filled)

        // Стилізація кнопки "Купити"
        holder.buttonBuy.backgroundTintList = null
        holder.buttonBuy.setBackgroundResource(R.drawable.button_red)
        holder.buttonBuy.setTextColor(holder.itemView.resources.getColor(android.R.color.white, null))

        // Логіка покупки товару
        holder.buttonBuy.setOnClickListener {
            scope.launch {
                withContext(Dispatchers.IO) {
                    val cartItems = cartItemDao.getCartItemsForUser(userId)
                    val existingItem = cartItems.find { it.productId == product.id }

                    if (existingItem != null) {
                        val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
                        cartItemDao.insert(updatedItem)
                    } else {
                        val newItem = CartItem(
                            userId = userId,
                            productId = product.id ?: 0,
                            quantity = 1
                        )
                        cartItemDao.insert(newItem)
                    }
                }

                // Показуємо повідомлення користувачу
                withContext(Dispatchers.Main) {
                    Toast.makeText(holder.itemView.context, "Товар додано до корзини", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Видалення з вішліста
        holder.imageViewRemove.setOnClickListener {
            scope.launch {
                val wishlistItem = withContext(Dispatchers.IO) {
                    wishlistDao.getWishlistItem(userId, product.id ?: 0)
                }
                if (wishlistItem != null) {
                    withContext(Dispatchers.IO) {
                        wishlistDao.delete(wishlistItem)
                    }
                    // Оновлюємо UI після видалення товару
                    withContext(Dispatchers.Main) {
                        productList = productList.filterNot { it.id == product.id }
                        notifyDataSetChanged()
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
