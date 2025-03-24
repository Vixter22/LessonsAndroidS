package com.example.log_reg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.log_reg.data.CartDisplayItem

class OrderAdapter(
    private var orderItems: List<CartDisplayItem>
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewOrderProduct)
        val textViewProductName: TextView = itemView.findViewById(R.id.textViewOrderProductName)
        val textViewProductPrice: TextView = itemView.findViewById(R.id.textViewOrderProductPrice)
        val textViewOrderQuantity: TextView = itemView.findViewById(R.id.textViewOrderQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int = orderItems.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val item = orderItems[position]
        holder.textViewProductName.text = item.product.name
        val totalPrice = item.product.price * item.cartItem.quantity
        holder.textViewProductPrice.text = "Ціна: $totalPrice грн"
        holder.textViewOrderQuantity.text = "Кількість: ${item.cartItem.quantity}"

        if (item.product.image.isNotEmpty()) {
            Glide.with(holder.itemView)
                .load(item.product.image)
                .into(holder.imageViewProduct)
        } else {
            holder.imageViewProduct.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    fun updateList(newList: List<CartDisplayItem>) {
        orderItems = newList
        notifyDataSetChanged()
    }
}
