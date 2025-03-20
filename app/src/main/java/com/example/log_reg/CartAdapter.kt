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
    private val onDeleteClick: (CartDisplayItem) -> Unit  // callback для видалення
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewCartProduct)
        val textViewProductName: TextView = itemView.findViewById(R.id.textViewCartProductName)
        val textViewProductPrice: TextView = itemView.findViewById(R.id.textViewCartProductPrice)
        val textViewCartQuantity: TextView = itemView.findViewById(R.id.textViewCartQuantity)
        val imageViewDelete: ImageView = itemView.findViewById(R.id.imageViewDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int = cartDisplayItems.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartDisplayItems[position]
        holder.textViewProductName.text = item.product.name
        holder.textViewProductPrice.text = "${item.product.price} грн"
        holder.textViewCartQuantity.text = "Кількість: ${item.cartItem.quantity}"
        if (item.product.image.isNotEmpty()) {
            Glide.with(holder.itemView)
                .load(item.product.image)
                .into(holder.imageViewProduct)
        } else {
            holder.imageViewProduct.setImageResource(R.drawable.ic_launcher_background)
        }

        // Обробка кліку на іконку видалення
        holder.imageViewDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    fun updateList(newList: List<CartDisplayItem>) {
        cartDisplayItems = newList
        notifyDataSetChanged()
    }
}
