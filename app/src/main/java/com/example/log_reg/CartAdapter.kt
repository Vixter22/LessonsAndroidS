package com.example.log_reg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.log_reg.data.CartDisplayItem

class CartAdapter(
    private var cartDisplayItems: List<CartDisplayItem>,
    private val onDeleteClick: (CartDisplayItem) -> Unit,
    private val onIncreaseClick: (CartDisplayItem) -> Unit,
    private val onDecreaseClick: (CartDisplayItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewCartProduct)
        val textViewProductName: TextView = itemView.findViewById(R.id.textViewCartProductName)
        val textViewProductPrice: TextView = itemView.findViewById(R.id.textViewCartProductPrice)
        val textViewCartQuantity: TextView = itemView.findViewById(R.id.textViewCartQuantity)
        val imageViewDelete: ImageView = itemView.findViewById(R.id.imageViewDelete)
        val imageViewPlus: ImageView = itemView.findViewById(R.id.imageViewPlus)
        val imageViewMinus: ImageView = itemView.findViewById(R.id.imageViewMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int = cartDisplayItems.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartDisplayItems[position]
        holder.textViewProductName.text = item.product.name

        // Обчислюємо загальну вартість для цього товару
        val totalPrice = item.product.price * item.cartItem.quantity
        holder.textViewProductPrice.text = "Ціна: $totalPrice грн"

        holder.textViewCartQuantity.text = item.cartItem.quantity.toString()

        if (item.product.image.isNotEmpty()) {
            Glide.with(holder.itemView)
                .load(item.product.image)
                .into(holder.imageViewProduct)
        } else {
            holder.imageViewProduct.setImageResource(R.drawable.ic_launcher_background)
        }

        // Логіка для кнопки зменшення: якщо кількість = 1, робимо її неактивною
        if (item.cartItem.quantity <= 1) {
            holder.imageViewMinus.alpha = 0.5f
            holder.imageViewMinus.isEnabled = false
        } else {
            holder.imageViewMinus.alpha = 1.0f
            holder.imageViewMinus.isEnabled = true
        }

        // Обробка кліку для збільшення кількості
        holder.imageViewPlus.setOnClickListener {
            onIncreaseClick(item)
        }

        // Обробка кліку для зменшення кількості
        holder.imageViewMinus.setOnClickListener {
            // Якщо кнопка активна, викликаємо onDecreaseClick
            if (holder.imageViewMinus.isEnabled) {
                onDecreaseClick(item)
            }
        }

        // Обробка кліку для видалення товару
        holder.imageViewDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    fun updateList(newList: List<CartDisplayItem>) {
        cartDisplayItems = newList
        notifyDataSetChanged()
    }
}
